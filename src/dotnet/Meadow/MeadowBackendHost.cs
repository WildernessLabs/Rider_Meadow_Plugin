extern alias MicrosoftLogger;

using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using JetBrains.Core;
using JetBrains.DataFlow;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Rd.Tasks;
using JetBrains.ReSharper.Feature.Services.Protocol;
using JetBrains.Util;
using JetBrains.Util.Logging;
using Meadow.CLI.Core.DeviceManagement;
using Meadow.CLI.Core.Devices;
using Meadow.Deployment;
using Meadow.Generated;
using MicrosoftLogger::Microsoft.Extensions.Logging;
using ILogger = MicrosoftLogger::Microsoft.Extensions.Logging.ILogger;


namespace Meadow;

[SolutionComponent]
public class MeadowBackendHost
{
    private static readonly JetBrains.Util.ILogger OurLogger = Logger.GetLogger<MeadowDeploymentProvider>();
    private static readonly MyLogger MicrosoftLoggerWrapper = new();

    public MeadowBackendHost(ISolution solution)
    {
        var meadowPluginModel = solution.GetProtocolSolution().GetMeadowPluginModel();
        meadowPluginModel.GetSerialPorts.SetAsync(GetSerialPortsAsync);
        meadowPluginModel.ResetDevice.SetAsync(ResetDevice);
    }


    private static async Task<List<string>> GetSerialPortsAsync(Lifetime lifetime, Unit unit)
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

    private static async Task<Unit> ResetDevice(Lifetime lifetime, string port)
    {
        try
        {
            var meadowDevice = await MeadowDeviceManager.GetMeadowForSerialPort(port, true, MicrosoftLoggerWrapper);
            if (meadowDevice == null) return Unit.Instance;
            using var helper = new MeadowDeviceHelper(meadowDevice, MicrosoftLoggerWrapper);
            await helper.ResetMeadow(lifetime);
        }
        catch (Exception e)
        {
            OurLogger.Error(e);
        }

        return Unit.Instance;
    }

    private class MyLogger : ILogger
    {
        public void Log<TState>(LogLevel logLevel, EventId eventId, TState state, Exception? exception, Func<TState, Exception?, string> formatter)
        {
            switch (logLevel)
            {
                case LogLevel.Trace:
                case LogLevel.Debug:
                case LogLevel.Information:
                    OurLogger.Info(formatter(state, exception));
                    break;
                case LogLevel.Warning:
                    OurLogger.Warn(formatter(state, exception));
                    break;
                case LogLevel.Error:
                case LogLevel.Critical:
                    OurLogger.Error(formatter(state, exception));
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
}