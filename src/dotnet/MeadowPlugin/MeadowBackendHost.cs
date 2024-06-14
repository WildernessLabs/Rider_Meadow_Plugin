using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using JetBrains.Collections.Viewable;
using JetBrains.Core;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Tasks;
using JetBrains.ReSharper.Feature.Services.Protocol;
using JetBrains.Util;
using JetBrains.Util.Logging;
using Meadow.CLI.Core.DeviceManagement;
using Meadow.CLI.Core.Devices;
using Meadow.CLI.Core.Internals.MeadowCommunication;
using MeadowPlugin.Deployment;
using MeadowPlugin.Model;
using ILogger = JetBrains.Util.ILogger;

namespace MeadowPlugin;

[SolutionComponent]
public class MeadowBackendHost
{
    private readonly Lifetime _solutionLifetime;
    private static readonly ILogger OurLogger = Logger.GetLogger<MeadowDeploymentProvider>();
    private readonly MeadowPluginModel _meadowPluginModel;

    private readonly Dictionary<string, AppRunSession> _runSessions = new();

    public MeadowBackendHost(ISolution solution, Lifetime solutionLifetime)
    {
        _solutionLifetime = solutionLifetime;
        _meadowPluginModel = solution.GetProtocolSolution().GetMeadowPluginModel();
        _meadowPluginModel.GetSerialPorts.SetAsync(GetSerialPortsAsync);
    }

    private static async Task<List<string>> GetSerialPortsAsync(Lifetime lifetime, Unit _)
    {
        try
        {
            return (await MeadowDeviceManager.GetSerialPorts()).AsList();
        }
        catch (Exception e)
        {
            OurLogger.Error(e);
            return EmptyList<string>.InstanceList.AsList();
        }
    }

    public async Task RegisterAppSessionAsync(string serialPort, int debugPort)
    {
        var meadowActionsLogger = new MeadowActionsLogger();
        var device =
            await MeadowDeviceManager.GetMeadowForSerialPort(serialPort, false, meadowActionsLogger);
        if (device == null)
        {
            throw new ArgumentException(
                "A device has not been selected. Please attach a device, then select it from the Device list.");
        }

        var helper = new MeadowDeviceHelper(device, meadowActionsLogger);
        
        DropSessionForSerialPort(serialPort);

        var model = new AppRunSessionModel();

        var sessionLifetimeDef = _solutionLifetime.CreateNested();
        if (debugPort > 0)
        {
            await helper.StartDebuggingSession(debugPort, sessionLifetimeDef.Lifetime);
        }

        var appRunSession = new AppRunSession(serialPort, helper, model, sessionLifetimeDef);
        
        _runSessions.Add(appRunSession.SerialPort, appRunSession);
        model.Terminate.AdviseOnce(sessionLifetimeDef.Lifetime, _ => { DropSessionForSerialPort(serialPort); });
        
        _meadowPluginModel.RunSessions.Add(sessionLifetimeDef.Lifetime, KeyValuePair.Create(serialPort, model));
    }

    private void DropSessionForSerialPort(string serialPort)
    {
        _runSessions.TryGetValue(serialPort)?.Terminate();
        _runSessions.Remove(serialPort);
    }
}

internal class AppRunSession
{
    private readonly AppRunSessionModel _model;
    private readonly LifetimeDefinition _lifetimeDefinition;

    public AppRunSession(string serialPort, MeadowDeviceHelper deviceHelper, AppRunSessionModel model,
        LifetimeDefinition lifetimeDefinition)
    {
        SerialPort = serialPort;
        _model = model;
        _lifetimeDefinition = lifetimeDefinition;
        deviceHelper.MeadowDevice.DataProcessor.OnReceiveData += OnReceiveData;

        _lifetimeDefinition.Lifetime.OnTermination(() =>
        {
            deviceHelper.MeadowDevice.DataProcessor.OnReceiveData -= OnReceiveData;
            deviceHelper.MonoDisable().Wait();
            deviceHelper.Dispose();
        });
    }

    public string SerialPort { get; }
    public Lifetime Lifetime => _lifetimeDefinition.Lifetime;

    private void OnReceiveData(object? sender, MeadowMessageEventArgs e)
    {
        if (e.MessageType != MeadowMessageType.AppOutput) return;
        _model.OutputReceived(e.Message);
    }

    public void Terminate()
    {
        _lifetimeDefinition.Terminate();
    }
}