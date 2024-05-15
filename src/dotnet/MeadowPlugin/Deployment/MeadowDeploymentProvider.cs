using System;
using System.IO;
using System.Threading.Tasks;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Base;
using JetBrains.ReSharper.Resources.Shell;
using JetBrains.Rider.Backend.Features.DeploymentHost.DeploymentProviders;
using JetBrains.Rider.Model;
using JetBrains.Threading;
using JetBrains.Util;
using JetBrains.Util.Logging;
using MeadowPlugin.Model;

namespace MeadowPlugin.Deployment;

[SolutionComponent]
public class MeadowDeploymentProvider(MeadowCliExecutor cliExecutor) : IDeploymentProvider
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
            var result = await GetDeploymentResult(deploymentSession, lifetime, meadowDeploymentArgs);
            lifetime.StartMainUnguarded(() =>
            {
                deploymentSession.Result.Set(result);
            }).NoAwait();
        });
    }

    private async Task<MeadowDeploymentResult> GetDeploymentResult(DeploymentSession deploymentSession, Lifetime lifetime,
        MeadowDeploymentArgs meadowDeploymentArgs)
    {
        var deploymentLogger = new DeploymentLogger(deploymentSession);
        try
        {

            var appPath = meadowDeploymentArgs.AppPath;
            if (!File.Exists(appPath))
            {
                deploymentSession.OutputAdded(new OutputMessage($"Deployment path '{appPath}' does not exist.",
                    DeployMessageKind.Error));
                return new MeadowDeploymentResult(DeploymentResultStatus.Failed);
            }

            var exitCode = await cliExecutor.ExecuteMeadowCommandForSerialPort(
                meadowDeploymentArgs.SerialPort,
                [
                    "app",
                    "deploy",
                    "--file",CommandLineUtil.QuoteIfNeeded(meadowDeploymentArgs.AppPath),
                    "--includePdbs", meadowDeploymentArgs.Debug.ToString()
                ], lifetime,
                deploymentLogger.OnOutputAvailable,
                deploymentLogger.OnErrorAvailable);

            if (exitCode != 0)
            {
                deploymentSession.OutputAdded(new OutputMessage("Deployment failed.", DeployMessageKind.Error));
                return new MeadowDeploymentResult(DeploymentResultStatus.Failed);
            }
            
            await cliExecutor.ExecuteMeadowCommandForSerialPort(
                meadowDeploymentArgs.SerialPort,
                [
                    "mono",
                    "enable"
                ], lifetime,
                deploymentLogger.OnOutputAvailable,
                deploymentLogger.OnErrorAvailable);
            
            return new MeadowDeploymentResult(DeploymentResultStatus.Success);
        }
        catch (TaskCanceledException)
        {
            return new MeadowDeploymentResult(DeploymentResultStatus.Cancelled);
        }
        catch (Exception e)
        {
            OurLogger.Error(e);
            return new MeadowDeploymentResult(DeploymentResultStatus.Failed);
        }
    }
}