using System;
using System.IO;
using System.Threading.Tasks;
using JetBrains.DataFlow;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Base;
using JetBrains.ReSharper.Resources.Shell;
using JetBrains.Rider.Backend.Features.DeploymentHost.DeploymentProviders;
using JetBrains.Rider.Model;
using JetBrains.Threading;
using JetBrains.Util;
using JetBrains.Util.Logging;
using Meadow.CLI.Core;
using Meadow.CLI.Core.DeviceManagement;
using Meadow.CLI.Core.Devices;
using MeadowPlugin.Model;
using Microsoft.Extensions.Logging;
using ILogger = JetBrains.Util.ILogger;

namespace MeadowPlugin.Deployment;

[SolutionComponent]
public class MeadowDeploymentProvider(MeadowBackendHost meadowBackendHost) : IDeploymentProvider
{
    private static readonly ILogger OurLogger = Logger.GetLogger<MeadowDeploymentProvider>();

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
                await meadowBackendHost.DropSessionForSerialPort(meadowDeploymentArgs.Device.SerialPort);
                result = await GetDeploymentResult(deploymentSession, lifetime, meadowDeploymentArgs);
                if (result.Status == DeploymentResultStatus.Success)
                {
                    await meadowBackendHost.RegisterAppSessionAsync(meadowDeploymentArgs.Device.SerialPort,
                        meadowDeploymentArgs.DebugPort);
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
        var deploymentSessionLogger = new DeploymentSessionLogger(deploymentSession);
        var appPath = meadowDeploymentArgs.AppPath;
        if (!File.Exists(appPath))
        {
            deploymentSession.OutputAdded(new OutputMessage($"Deployment path '{appPath}' does not exist.",
                DeployMessageKind.Error));
            return new MeadowDeploymentResult(DeploymentResultStatus.Failed);
        }

        var device = await MeadowDeviceManager.GetMeadowForSerialPort(meadowDeploymentArgs.Device.SerialPort, false,
            deploymentSessionLogger);
        if (device == null)
        {
            deploymentSession.OutputAdded(new OutputMessage(
                "A device has not been selected. Please attach a device, then select it from the Device list.",
                DeployMessageKind.Error));
            return new MeadowDeploymentResult(DeploymentResultStatus.Failed);
        }

        using var helper = new MeadowDeviceHelper(device, deploymentSessionLogger);

        var osVersion = await helper.GetOSVersion(TimeSpan.FromSeconds(30), lifetime);

        try
        {
            // make sure we have the same locally because we will do linking/trimming against that runtime
            await new DownloadManager(deploymentSessionLogger).DownloadOsBinaries(osVersion, false, lifetime);
        }
        catch
        {
            //OS binaries failed to download
            //Either no internet connection or we're depoying to a pre-release OS version 
            deploymentSessionLogger.LogWarning("Meadow assemblies download failed, using local copy");
        }

        await helper.DeployApp(meadowDeploymentArgs.AppPath, meadowDeploymentArgs.DebugPort > 0, lifetime, true);

        return new MeadowDeploymentResult(DeploymentResultStatus.Success);
    }
}

class DeploymentSessionLogger(DeploymentSession deploymentSession) : Microsoft.Extensions.Logging.ILogger
{
    public void Log<TState>(LogLevel logLevel, EventId eventId, TState state, Exception? exception,
        Func<TState, Exception?, string> formatter)
    {
        switch (logLevel)
        {
            case LogLevel.Trace:
            case LogLevel.Debug:
            case LogLevel.Information:
                deploymentSession.OutputAdded(new OutputMessage(formatter(state, exception), DeployMessageKind.Info));
                break;
            case LogLevel.Warning:
                deploymentSession.OutputAdded(new OutputMessage(formatter(state, exception),
                    DeployMessageKind.Warning));
                break;
            case LogLevel.Error:
            case LogLevel.Critical:
                deploymentSession.OutputAdded(new OutputMessage(formatter(state, exception), DeployMessageKind.Error));
                break;
            case LogLevel.None:
                break;
            default:
                throw new ArgumentOutOfRangeException(nameof(logLevel), logLevel, null);
        }
    }

    public bool IsEnabled(LogLevel logLevel)
    {
        return true;
    }

    public IDisposable BeginScope<TState>(TState state) where TState : notnull
    {
        return new Disposable.EmptyDisposable();
    }
}