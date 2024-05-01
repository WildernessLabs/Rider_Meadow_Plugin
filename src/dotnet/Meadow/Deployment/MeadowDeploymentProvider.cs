extern alias MicrosoftLogger;

using System;
using System.IO;
using System.Threading.Tasks;
using JetBrains.Application.Threading;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rider.Backend.Features.DeploymentHost.DeploymentProviders;
using JetBrains.Rider.Model;
using JetBrains.Threading;
using Meadow.CLI.Core;
using Meadow.CLI.Core.DeviceManagement;
using Meadow.CLI.Core.Devices;
using Meadow.Generated;
using MicrosoftLogger::Microsoft.Extensions.Logging;

namespace Meadow.Deployment;

[SolutionComponent]
public class MeadowDeploymentProvider(IShellLocks locks) : IDeploymentProvider
{
    private static async Task DownloadOsSafe(Lifetime lifetime, MeadowDeviceHelper helper, DeploymentLogger deploymentLogger)
    {
        try
        {
            var osVersion = await helper.GetOSVersion(TimeSpan.FromSeconds(30), lifetime);

            await new DownloadManager(deploymentLogger).DownloadOsBinaries(osVersion, cancellationToken: lifetime);
        }
        catch
        {
            deploymentLogger.LogWarning("OS download failed, make sure you have an active internet connection");
        }
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

        locks.StartBackground(lifetime, async () =>
        {
            var deploymentLogger = new DeploymentLogger(deploymentSession);
            var debuggingPort = -1;

            try
            {
                var meadowDevice =
                    await MeadowDeviceManager.GetMeadowForSerialPort(meadowDeploymentArgs.Port, true, deploymentLogger);
                if (meadowDevice == null)
                {
                    deploymentLogger.LogError(
                        "A device has not been selected. Please attach a device, then select it from the Device list.");
                    return new MeadowDeploymentResult(debuggingPort, DeploymentResultStatus.Failed);
                }

                var appPath = meadowDeploymentArgs.AppPath;
                if (!File.Exists(appPath))
                {
                    deploymentLogger.LogError("Deployment path '{path}' does not exist.", appPath);
                    return new MeadowDeploymentResult(debuggingPort, DeploymentResultStatus.Failed);
                }

                using var helper = new MeadowDeviceHelper(meadowDevice, deploymentLogger);

                await DownloadOsSafe(lifetime, helper, deploymentLogger);

                try
                {
                    await helper.DeployApp(appPath, meadowDeploymentArgs.Debug, lifetime);
                }
                finally
                {
                    var running = await helper.GetMonoRunState(lifetime);
                    if (!running)
                    {
                        await helper.MonoEnable(true, lifetime);
                    }
                }

                if (meadowDeploymentArgs.Debug)
                {
                    debuggingPort = MeadowPortUtil.GetNextDebuggingPort();
                    await helper.StartDebuggingSession(debuggingPort, lifetime);
                }

                return new MeadowDeploymentResult(debuggingPort, DeploymentResultStatus.Success);
            }
            catch (Exception e)
            {
                deploymentLogger.LogError(e, "Deployment failed.");
                return new MeadowDeploymentResult(debuggingPort, DeploymentResultStatus.Failed);
            }
        }).NoAwait();
    }
}