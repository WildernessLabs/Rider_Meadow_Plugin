using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using JetBrains.Core;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Tasks;
using JetBrains.ReSharper.Feature.Services.Protocol;
using JetBrains.Util;
using JetBrains.Util.Logging;
using MeadowPlugin.Deployment;
using MeadowPlugin.Model;

namespace MeadowPlugin;

[SolutionComponent]
public class MeadowBackendHost
{
    private readonly MeadowCliExecutor _cliExecutor;
    private readonly Lifetime _lifetime;
    private static readonly ILogger OurLogger = Logger.GetLogger<MeadowDeploymentProvider>();

    public MeadowBackendHost(ISolution solution, MeadowCliExecutor cliExecutor, Lifetime lifetime)
    {
        _cliExecutor = cliExecutor;
        _lifetime = lifetime;
        var meadowPluginModel = solution.GetProtocolSolution().GetMeadowPluginModel();
        meadowPluginModel.GetSerialPorts.SetAsync(GetSerialPortsAsync);
        meadowPluginModel.StartDebugServer.Set(StartDebuggingServer);
        meadowPluginModel.Terminate.Set(TerminateAsync);
    }


    private async Task<List<string>> GetSerialPortsAsync(Lifetime lifetime, CliRunnerInfo runnerInfo)
    {
        try
        {
            const string portPrefix = "Found Meadow: ";
            var ports = new List<string>();
            var errorOutput = new StringBuilder();
            await _cliExecutor.ExecuteMeadowCommand(["list", "ports"], runnerInfo.CliPath, lifetime,
                output =>
                {
                    if (output.StartsWith(portPrefix))
                    {
                        ports.Add(output.Substring(portPrefix.Length));
                    }
                },
                error => { errorOutput.AppendLine(error); });

            if (!errorOutput.IsEmpty())
            {
                OurLogger.Error($"Error while fetching serial ports list: {errorOutput}");
            }

            return ports;
        }
        catch (Exception e)
        {
            OurLogger.Error(e);
            return EmptyList<string>.InstanceList.AsList();
        }
    }

    private Unit StartDebuggingServer(Lifetime lifetime, DebugServerInfo debugServerInfo)
    {
        _cliExecutor.ExecuteMeadowCommandForSerialPort(
            debugServerInfo.RunnerInfo.SerialPort,
            ["debug", "--DebugPort", debugServerInfo.DebugPort.ToString()],
            debugServerInfo.RunnerInfo.CliPath,
            _lifetime);
        return Unit.Instance;
    }

    private async Task<Unit> TerminateAsync(Lifetime lifetime, CliRunnerInfoOnPort runnerInfo)
    {
        await _cliExecutor.ExecuteMeadowCommandForSerialPort(
            runnerInfo.SerialPort,
            ["mono", "disable"],
            runnerInfo.CliPath,
            lifetime);
        return Unit.Instance;
    }
}