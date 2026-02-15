using System;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using JetBrains.Application.Parts;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Base;
using JetBrains.ReSharper.Resources.Shell;
using JetBrains.Rider.Backend.Features.DeploymentHost.DeploymentProviders;
using JetBrains.Rider.Model;
using JetBrains.Threading;
using JetBrains.Util;
using JetBrains.Util.Logging;
using MeadowPlugin.Logging;
using MeadowPlugin.Model;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using ILogger = JetBrains.Util.ILogger;

namespace MeadowPlugin.Deployment;

[SolutionComponent(Instantiation.DemandAnyThreadUnsafe)]
public class MeadowDeploymentProvider : IDeploymentProvider
{
    private static readonly ILogger OurLogger = Logger.GetLogger<MeadowDeploymentProvider>();
    private DeploymentSessionLogger? _deploymentSessionLogger;
    private readonly MeadowBackendHost _meadowBackendHost;
    private int _dapSequence = 1;

    public MeadowDeploymentProvider(MeadowBackendHost meadowBackendHost)
    {
        _meadowBackendHost = meadowBackendHost;
    }

    private void LogInfo(string message)
    {
        _deploymentSessionLogger?.Log(LogLevel.Information, default, message, null, (msg, _) => msg);
    }

    private void LogError(string message)
    {
        _deploymentSessionLogger?.Log(LogLevel.Error, default, message, null, (msg, _) => msg);
    }

    public bool IsApplicable(DeploymentArgsBase args)
    {
        var isApplicable = args is MeadowDeploymentArgs;
        OurLogger.Info($"[MEADOW] IsApplicable called: args type={args?.GetType().Name}, result={isApplicable}");
        return isApplicable;
    }

    public void Deploy(DeploymentArgsBase args, DeploymentSession deploymentSession, Lifetime lifetime)
    {
        OurLogger.Info($"[MEADOW] Deploy called with args type: {args.GetType().Name}");
        
        if (args is not MeadowDeploymentArgs meadowDeploymentArgs)
        {
            throw new ArgumentException($"Unexpected deployment args: {args.GetType().Name}");
        }

        OurLogger.Info("[MEADOW] Starting background deployment task...");
        lifetime.StartBackground(async () =>
        {
            MeadowDeploymentResult result;
            try
            {
                OurLogger.Info("[MEADOW] Calling DeployViaDapAsync...");
                result = await DeployViaDapAsync(deploymentSession, lifetime, meadowDeploymentArgs);
            }
            catch (TaskCanceledException)
            {
                result = new MeadowDeploymentResult(DeploymentResultStatus.Cancelled);
            }
            catch (Exception e)
            {
                OurLogger.Error(e);
                result = new MeadowDeploymentResult(DeploymentResultStatus.Failed);
            }

            lifetime.StartMainUnguarded(() => { deploymentSession.Result.Set(result); }).NoAwait();
        });
    }

    /// <summary>
    /// Deploy via DAP adapter (meadow-debugging.exe) - unified approach across all IDEs.
    /// </summary>
    private async Task<MeadowDeploymentResult> DeployViaDapAsync(
        DeploymentSession deploymentSession,
        Lifetime lifetime,
        MeadowDeploymentArgs meadowDeploymentArgs)
    {
        OurLogger.Info("[MEADOW] === DEPLOYMENT STARTED (DAP) ===");
        _deploymentSessionLogger = new DeploymentSessionLogger(deploymentSession);

        // Locate DAP adapter executable
        var pluginPath = _meadowBackendHost.GetType().Assembly.Location;
        var adapterPath = Path.Combine(Path.GetDirectoryName(pluginPath)!, "DapAdapter", "meadow-debugging.exe");
        OurLogger.Info($"[MEADOW] Plugin path: {pluginPath}");
        OurLogger.Info($"[MEADOW] Adapter path: {adapterPath}");

        if (!File.Exists(adapterPath))
        {
            LogError($"DAP adapter not found at: {adapterPath}");
            return new MeadowDeploymentResult(DeploymentResultStatus.Failed);
        }

        // Generate MSBuild property file for adapter
        var appPath = meadowDeploymentArgs.AppPath;
        var outputPath = Path.GetDirectoryName(appPath) ?? ".";
        var assemblyName = Path.GetFileNameWithoutExtension(appPath);
        var propsFile = Path.Combine(Path.GetTempPath(), $"meadow_deploy_{Guid.NewGuid():N}.props");
        File.WriteAllText(propsFile, $"OutputPath={outputPath}{Path.DirectorySeparatorChar}\nAssemblyName={assemblyName}\n");

        var projectPath = Path.GetDirectoryName(meadowDeploymentArgs.ProjectFilePath) ?? ".";
        var configuration = "Debug"; // Could be extracted from args if needed

        try
        {
            var processInfo = new ProcessStartInfo
            {
                FileName = adapterPath,
                UseShellExecute = false,
                CreateNoWindow = true,
                RedirectStandardInput = true,
                RedirectStandardOutput = true,
                RedirectStandardError = true
            };

            using var process = Process.Start(processInfo);
            if (process == null)
            {
                LogError("Failed to start DAP adapter process");
                return new MeadowDeploymentResult(DeploymentResultStatus.Failed);
            }

            OurLogger.Info($"[MEADOW] DAP adapter process started, PID: {process.Id}");

            // Initialize DAP protocol
            OurLogger.Info("[MEADOW] Sending initialize request...");
            await SendDapRequestAsync(process.StandardInput, "initialize", new
            {
                clientID = "rider",
                adapterID = "meadow",
                linesStartAt1 = true,
                columnsStartAt1 = true,
                pathFormat = "path"
            });

            // Launch with debugPort from args
            OurLogger.Info($"[MEADOW] Sending launch request with debugPort: {meadowDeploymentArgs.DebugPort}, serial: {meadowDeploymentArgs.Device.SerialPort}");
            await SendDapRequestAsync(process.StandardInput, "launch", new
            {
                type = "meadow",
                request = "launch",
                projectPath = projectPath,
                projectConfiguration = configuration,
                serial = meadowDeploymentArgs.Device.SerialPort,
                msbuildPropertyFile = propsFile,
                debugPort = meadowDeploymentArgs.DebugPort
            });

            // Monitor DAP events
            OurLogger.Info("[MEADOW] Starting DAP event monitoring...");
            var success = await MonitorDapDeploymentAsync(process, lifetime);
            OurLogger.Info($"[MEADOW] DAP monitoring completed, success: {success}");

            // Disconnect
            await SendDapRequestAsync(process.StandardInput, "disconnect", new { });

            if (!process.HasExited)
            {
                await Task.Run(() => process.WaitForExit(5000));
            }

            return success
                ? new MeadowDeploymentResult(DeploymentResultStatus.Success)
                : new MeadowDeploymentResult(DeploymentResultStatus.Failed);
        }
        catch (Exception ex)
        {
            OurLogger.Error(ex, "DAP deployment failed");
            LogError($"Deployment error: {ex.Message}");
            return new MeadowDeploymentResult(DeploymentResultStatus.Failed);
        }
        finally
        {
            // Cleanup temp file
            try { if (File.Exists(propsFile)) File.Delete(propsFile); }
            catch { /* Ignore cleanup errors */ }
        }
    }

    private async Task SendDapRequestAsync(StreamWriter stdin, string command, object arguments)
    {
        var request = new
        {
            seq = _dapSequence++,
            type = "request",
            command = command,
            arguments = arguments
        };

        var json = JsonConvert.SerializeObject(request);
        var content = Encoding.UTF8.GetBytes(json);

        var header = $"Content-Length: {content.Length}\r\n\r\n";
        await stdin.WriteAsync(header);
        await stdin.WriteAsync(json);
        await stdin.FlushAsync();
    }

    private async Task<bool> MonitorDapDeploymentAsync(Process process, Lifetime lifetime)
    {
        bool deploymentComplete = false;
        bool deploymentSuccess = true;
        int messageCount = 0;

        OurLogger.Info("[MEADOW] MonitorDapDeploymentAsync started");

        while (!process.HasExited && !lifetime.IsTerminated)
        {
            var line = await process.StandardOutput.ReadLineAsync();
            if (line == null)
            {
                OurLogger.Info("[MEADOW] Reached end of stream");
                break;
            }

            // DAP messages are preceded by Content-Length header
            if (line.StartsWith("Content-Length:", StringComparison.OrdinalIgnoreCase))
            {
                await process.StandardOutput.ReadLineAsync(); // Skip blank line
                var contentLength = int.Parse(line.Substring(15).Trim());

                // Read JSON message
                var messageBuffer = new char[contentLength];
                await process.StandardOutput.ReadAsync(messageBuffer, 0, contentLength);
                var messageJson = new string(messageBuffer);

                try
                {
                    messageCount++;
                    var message = JObject.Parse(messageJson);
                    var messageType = message["type"]?.ToString();
                    
                    OurLogger.Info($"[MEADOW] Message #{messageCount}: type={messageType}");

                    if (messageType == "event")
                    {
                        var eventType = message["event"]?.ToString();
                        OurLogger.Info($"[MEADOW] Event received: {eventType}");
                        var body = message["body"] as JObject;
                        HandleDapEvent(eventType, body);

                        if (eventType == "terminated" || eventType == "exited")
                        {
                            deploymentComplete = true;
                            break;
                        }
                    }
                    else if (messageType == "response")
                    {
                        var command = message["command"]?.ToString();
                        var success = message["success"]?.ToObject<bool>() ?? false;

                        if (command == "launch" && !success)
                        {
                            var errorMsg = message["message"]?.ToString() ?? "Unknown error";
                            LogError($"Launch failed: {errorMsg}");
                            deploymentSuccess = false;
                            break;
                        }
                    }
                }
                catch (JsonException ex)
                {
                    OurLogger.Warn($"[MEADOW] Failed to parse DAP message: {ex.Message}");
                }
            }
        }

        OurLogger.Info($"[MEADOW] MonitorDapDeploymentAsync completed: {messageCount} messages, success={deploymentSuccess}, complete={deploymentComplete}");
        return deploymentSuccess && deploymentComplete;
    }

    private void HandleDapEvent(string eventType, JObject body)
    {
        OurLogger.Info($"[MEADOW] HandleDapEvent called: eventType={eventType}");
        
        switch (eventType)
        {
            case "output":
                var output = body?["output"]?.ToString();
                if (!string.IsNullOrEmpty(output))
                {
                    LogInfo(output.TrimEnd());
                }
                break;

            case "progressStart":
                var title = body?["title"]?.ToString() ?? "Deployment";
                LogInfo($"[Progress] {title}");
                break;

            case "progressUpdate":
                var message = body?["message"]?.ToString() ?? "";
                var percentage = body?["percentage"]?.ToObject<int>() ?? 0;
                var fileName = body?["progressId"]?.ToString() ?? "App.dll";
                
                LogInfo($"[Progress] {percentage}% - {message}");
                
                // Fire RD signal for Rider UI notifications
                var status = percentage >= 100 ? "Complete" : "Deploying";
                OurLogger.Info($"[MEADOW] Firing progressUpdate signal: {fileName} - {percentage}% - {status}");
                _meadowBackendHost.Model.ProgressUpdate.Fire(new ProgressUpdate(fileName, percentage, status));
                break;

            case "progressEnd":
                var endMessage = body?["message"]?.ToString() ?? "Deployment complete";
                LogInfo($"[Progress] {endMessage}");
                break;
        }
    }
}