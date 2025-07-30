using System;
using System.IO;
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
using Meadow.CLI;
using Meadow.CLI.Commands.DeviceManagement;
using Meadow.Hcom;
using Meadow.Package;
using Meadow.Software;
using MeadowPlugin.Logging;
using MeadowPlugin.Model;
using Microsoft.Extensions.Logging;
using ILogger = JetBrains.Util.ILogger;

namespace MeadowPlugin.Deployment;

[SolutionComponent(Instantiation.DemandAnyThreadUnsafe)]
public class MeadowDeploymentProvider : IDeploymentProvider
{
    private static readonly ILogger OurLogger = Logger.GetLogger<MeadowDeploymentProvider>();
    private IMeadowConnection _meadowConnection;
    private DeploymentSessionLogger? _deploymentSessionLogger;
    private readonly MeadowBackendHost _meadowBackendHost;

    private readonly SettingsManager _settingsManager = new SettingsManager();
    private readonly MeadowConnectionManager _connectionManager;

    public MeadowDeploymentProvider(MeadowBackendHost meadowBackendHost)
    {
        _meadowBackendHost = meadowBackendHost;
        _connectionManager = new MeadowConnectionManager(_settingsManager);
    }

    public bool IsApplicable(DeploymentArgsBase args)
    {
        return args is MeadowDeploymentArgs;
    }

    public void Deploy(DeploymentArgsBase args, DeploymentSession deploymentSession, Lifetime lifetime)
    {
        if (args is not MeadowDeploymentArgs meadowDeploymentArgs)
        {
            throw new ArgumentException($"Unexpected deployment args: {args.GetType().Name}");
        }

        lifetime.StartBackground(async () =>
        {
            MeadowDeploymentResult result;
            try
            {
                await _meadowBackendHost.DropSessionForSerialPort(meadowDeploymentArgs.Device.SerialPort);
                result = await GetDeploymentResult(deploymentSession, lifetime, meadowDeploymentArgs);
                if (result.Status == DeploymentResultStatus.Success)
                {
                    await _meadowBackendHost.RegisterAppSessionAsync(meadowDeploymentArgs.Device.SerialPort,
                        meadowDeploymentArgs.DebugPort, _meadowConnection);
                }
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

    private async Task<MeadowDeploymentResult> GetDeploymentResult(DeploymentSession deploymentSession,
        Lifetime lifetime,
        MeadowDeploymentArgs meadowDeploymentArgs)
    {
        _deploymentSessionLogger = new DeploymentSessionLogger(deploymentSession);

        return await Task.Run(async () =>
        {
            if (_meadowConnection != null)
            {
                _meadowConnection.FileWriteProgress -= MeadowConnection_DeploymentProgress;
                _meadowConnection.DeviceMessageReceived -= MeadowConnection_DeviceMessageReceived;
                _meadowConnection = null;
            }

            _meadowConnection = await _connectionManager.GetConnection(meadowDeploymentArgs.Device.SerialPort);

            if (_meadowConnection == null)
            {
                deploymentSession.OutputAdded(new OutputMessage(
                    "A device has not been selected. Please attach a device, then select it from the Device list.",
                    DeployMessageKind.Error));
                return new MeadowDeploymentResult(DeploymentResultStatus.Failed);
            }
            else
            {
                _meadowConnection.FileWriteProgress += MeadowConnection_DeploymentProgress;
                _meadowConnection.DeviceMessageReceived += MeadowConnection_DeviceMessageReceived;

                await _meadowConnection.WaitForMeadowAttach(lifetime);

                _deploymentSessionLogger.LogInformation("Checking runtime state...");
                if (await _meadowConnection.IsRuntimeEnabled(lifetime))
                {
                    _deploymentSessionLogger.LogInformation("Disabling runtime...");
                    await _meadowConnection.RuntimeDisable(lifetime);
                }

                var deviceInfo = await _meadowConnection.GetDeviceInfo(lifetime);
                string osVersion = deviceInfo?.OsVersion;
                _deploymentSessionLogger.LogInformation($"Found Meadow with OS v{osVersion}");

                var fileManager = new FileManager(null);
                await fileManager.Refresh();

                var isDebugging = meadowDeploymentArgs.DebugPort > 0;

                try
                {
                    var packageManager = new PackageManager(fileManager);

                    var appPath = meadowDeploymentArgs.AppPath;
                    if (!File.Exists(appPath))
                    {
                        _deploymentSessionLogger.LogInformation($"Deployment path '{appPath}' does not exist.");
                        return new MeadowDeploymentResult(DeploymentResultStatus.Failed);
                    }

                    if (!string.IsNullOrEmpty(appPath))
                    {
                        await packageManager.TrimApplication(new System.IO.FileInfo(appPath), osVersion, isDebugging,
                            null, _deploymentSessionLogger, lifetime);

                        var appFolder = Path.GetDirectoryName(appPath) ?? ".";
                        _deploymentSessionLogger.LogInformation("Deploying application...");
                        await AppManager.DeployApplication(packageManager, _meadowConnection, osVersion, appFolder,
                            isDebugging, false, _deploymentSessionLogger, lifetime);

                        await Task.Delay(1500);

                        await _meadowConnection.RuntimeEnable(lifetime);
                    }
                }
                catch (Exception e)
                {
                    _deploymentSessionLogger.LogError(e.Message);
                    return new MeadowDeploymentResult(DeploymentResultStatus.Failed);
                }
                finally
                {
                    _meadowConnection.FileWriteProgress -= MeadowConnection_DeploymentProgress;
                }

                return new MeadowDeploymentResult(DeploymentResultStatus.Success);
            }
        });
    }

    private async void MeadowConnection_DeviceMessageReceived(object sender, (string message, string source) e)
    {
        if (_deploymentSessionLogger != null)
        {
            await _deploymentSessionLogger.ReportDeviceMessage(e.source, e.message);
        }
    }

    private async void MeadowConnection_DeploymentProgress(object sender,
        (string fileName, long completed, long total) e)
    {
        var p = (uint)((e.completed / (double)e.total) * 100d);

        if (_deploymentSessionLogger != null)
        {
            await _deploymentSessionLogger.ReportFileProgress(e.fileName, p);
        }
    }
}