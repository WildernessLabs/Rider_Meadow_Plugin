using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using JetBrains.Collections.Viewable;
using JetBrains.Core;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Tasks;
using JetBrains.ReSharper.Feature.Services.Protocol;
using JetBrains.ReSharper.Resources.Shell;
using JetBrains.Threading;
using JetBrains.Util;
using JetBrains.Util.Logging;
using Meadow.CLI.Commands.DeviceManagement;
using Meadow.Hcom;
using MeadowPlugin.Deployment;
using MeadowPlugin.Logging;
using MeadowPlugin.Model;
using Microsoft.Extensions.Logging;
using ILogger = JetBrains.Util.ILogger;

namespace MeadowPlugin;

[SolutionComponent]
public class MeadowBackendHost
{
    private static readonly ILogger OurLogger = Logger.GetLogger<MeadowDeploymentProvider>();

    private readonly Lifetime _solutionLifetime;
    private readonly MeadowPluginModel _meadowPluginModel;

    private readonly Dictionary<string, AppRunSession> _runSessions = new();

    IMeadowConnection? _meadowConnection = null;
    private MeadowActionsLogger _meadowActionsLogger;

    public MeadowBackendHost(ISolution solution, Lifetime solutionLifetime)
    {
        _solutionLifetime = solutionLifetime;
        _meadowPluginModel = solution.GetProtocolSolution().GetMeadowPluginModel();
        _meadowPluginModel.GetSerialPorts.SetAsync(GetSerialPortsAsync);

        _meadowActionsLogger = new MeadowActionsLogger();
    }
    private static async Task<List<string>> GetSerialPortsAsync(Lifetime lifetime, Unit _)
    {
        try
        {
            var ports = await MeadowConnectionManager.GetSerialPorts();
            if (ports == null)
            {
                return EmptyList<string>.InstanceList.AsList();
            }
            else
            {
                return ports.AsList();
            }
        }
        catch (Exception e)
        {
            OurLogger.Error(e);
            return EmptyList<string>.InstanceList.AsList();
        }
    }

    public async Task RegisterAppSessionAsync(string serialPort, int debugPort)
    {
        _meadowConnection = await MeadowConnectionManager.GetConnectionForRoute(serialPort);

        if (_meadowConnection != null)
        {
            var model = new AppRunSessionModel();
            var sessionLifetimeDef = _solutionLifetime.CreateNested();
            model.Terminate.AdviseOnce(sessionLifetimeDef.Lifetime,
                _ => { DropSessionForSerialPort(serialPort).NoAwait(); });

            var isDebugging = debugPort > 0;

            // Debugger only returns when session is done
            if (isDebugging)
            {
                _meadowActionsLogger.LogInformation("Debugging...");
                await _meadowConnection.StartDebuggingSession(debugPort, _meadowActionsLogger, sessionLifetimeDef.Lifetime);
            }

            var appRunSession = new AppRunSession(serialPort, _meadowConnection, model, sessionLifetimeDef);

            _runSessions.Add(appRunSession.SerialPort, appRunSession);

            await _solutionLifetime.StartMainUnguarded(() =>
            {
                _meadowPluginModel.RunSessions.Add(sessionLifetimeDef.Lifetime, KeyValuePair.Create(serialPort, model));
            });
        }
    }

    public async Task DropSessionForSerialPort(string serialPort)
    {
        var appRunSession = _runSessions.TryGetValue(serialPort);
        if (appRunSession == null)
            return;
        _runSessions.Remove(serialPort);
        await appRunSession.TerminateAsync();
    }
}