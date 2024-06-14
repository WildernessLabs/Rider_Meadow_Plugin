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
using Meadow.CLI.Core.DeviceManagement;
using Meadow.CLI.Core.Devices;
using Meadow.CLI.Core.Internals.MeadowCommunication;
using MeadowPlugin.Deployment;
using MeadowPlugin.Model;
using Microsoft.Extensions.Logging.Abstractions;
using ILogger = JetBrains.Util.ILogger;

namespace MeadowPlugin;

[SolutionComponent]
public class MeadowBackendHost
{
    private static readonly ILogger OurLogger = Logger.GetLogger<MeadowDeploymentProvider>();

    private readonly Lifetime _solutionLifetime;
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

        var model = new AppRunSessionModel();
        var sessionLifetimeDef = _solutionLifetime.CreateNested();
        model.Terminate.AdviseOnce(sessionLifetimeDef.Lifetime,
            _ => { DropSessionForSerialPort(serialPort).NoAwait(); });

        if (debugPort > 0)
        {
            await helper.StartDebuggingSession(debugPort, sessionLifetimeDef.Lifetime);
        }

        var appRunSession = new AppRunSession(serialPort, helper, model, sessionLifetimeDef);

        _runSessions.Add(appRunSession.SerialPort, appRunSession);

        await _solutionLifetime.StartMainUnguarded(() =>
        {
            _meadowPluginModel.RunSessions.Add(sessionLifetimeDef.Lifetime, KeyValuePair.Create(serialPort, model));
        });
    }

    public async Task DropSessionForSerialPort(string serialPort)
    {
        var appRunSession = _runSessions.TryGetValue(serialPort);
        if (appRunSession == null) return;
        _runSessions.Remove(serialPort);
        await appRunSession.TerminateAsync();
    }
}

internal class AppRunSession
{
    private readonly MeadowDeviceHelper _deviceHelper;
    private readonly AppRunSessionModel _model;
    private readonly LifetimeDefinition _lifetimeDefinition;

    public AppRunSession(string serialPort, MeadowDeviceHelper deviceHelper, AppRunSessionModel model,
        LifetimeDefinition lifetimeDefinition)
    {
        SerialPort = serialPort;
        _deviceHelper = deviceHelper;
        _model = model;
        _lifetimeDefinition = lifetimeDefinition;
        deviceHelper.MeadowDevice.DataProcessor.OnReceiveData += OnReceiveData;

        _lifetimeDefinition.Lifetime.OnTermination(() =>
        {
            deviceHelper.MeadowDevice.DataProcessor.OnReceiveData -= OnReceiveData;
        });
    }

    public string SerialPort { get; }

    private void OnReceiveData(object? sender, MeadowMessageEventArgs e)
    {
        if (e.MessageType != MeadowMessageType.AppOutput) return;
        _model.OutputReceived(e.Message);
    }

    public async Task TerminateAsync()
    {
        _lifetimeDefinition.Terminate();
        _deviceHelper.Dispose();
        await TryDisableMono();
    }

    private async Task TryDisableMono()
    {
        var device = await MeadowDeviceManager.GetMeadowForSerialPort(SerialPort, false);
        if (device == null) return;
        using var helper = new MeadowDeviceHelper(device, NullLogger.Instance);
        await helper.MonoDisable();
    }
}