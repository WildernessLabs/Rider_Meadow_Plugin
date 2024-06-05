using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using JetBrains.Core;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Tasks;
using JetBrains.ReSharper.Feature.Services.Protocol;
using JetBrains.Threading;
using JetBrains.Util;
using JetBrains.Util.Logging;
using Meadow.CLI.Core.DeviceManagement;
using MeadowPlugin.Deployment;
using MeadowPlugin.Model;
using ILogger = JetBrains.Util.ILogger;

namespace MeadowPlugin;

[SolutionComponent]
public class MeadowBackendHost
{
    private readonly Lifetime _solutionLifetime;
    private readonly MeadowDevices _devices;
    private static readonly ILogger OurLogger = Logger.GetLogger<MeadowDeploymentProvider>();

    public MeadowBackendHost(ISolution solution, Lifetime solutionLifetime, MeadowDevices devices)
    {
        _solutionLifetime = solutionLifetime;
        _devices = devices;
        var meadowPluginModel = solution.GetProtocolSolution().GetMeadowPluginModel();
        meadowPluginModel.GetSerialPorts.SetAsync(GetSerialPortsAsync);
        meadowPluginModel.StartDebugServer.Set(StartDebuggingServer);
        meadowPluginModel.Terminate.Set(TerminateAsync);
    }


    private async Task<List<string>> GetSerialPortsAsync(Lifetime lifetime, Unit _)
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

    private Unit StartDebuggingServer(Lifetime lifetime, DebugServerInfo debugServerInfo)
    {
        var helper = _devices.GetDeviceHelper(debugServerInfo.Device.SerialPort, _solutionLifetime, new MeadowActionsLogger());
        if (helper == null)
        {
            throw new ArgumentException(
                "A device has not been selected. Please attach a device, then select it from the Device list.");
        }

        helper.StartDebuggingSession(debugServerInfo.DebugPort, _solutionLifetime).NoAwait();
        return Unit.Instance;
    }

    private async Task<Unit> TerminateAsync(Lifetime lifetime, DeviceModel device)
    {
        var helper = _devices.GetDeviceHelper(device.SerialPort, lifetime, new MeadowActionsLogger());
        if (helper == null)
        {
            throw new ArgumentException(
                "A device has not been selected. Please attach a device, then select it from the Device list.");
        }

        await helper.MonoDisable(false, lifetime);
        return Unit.Instance;
    }
}