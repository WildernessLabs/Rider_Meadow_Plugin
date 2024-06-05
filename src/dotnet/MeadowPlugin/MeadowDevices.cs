using System.Collections.Generic;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Util;
using Meadow.CLI.Core.DeviceManagement;
using Meadow.CLI.Core.Devices;
using ILogger = Microsoft.Extensions.Logging.ILogger;

namespace MeadowPlugin;

[SolutionComponent]
public class MeadowDevices(Lifetime solutionLifetime)
{
    private readonly object _devicesLock = new();
    private readonly Dictionary<string, MeadowDeviceHelper> _devices = new();

    public MeadowDeviceHelper? GetDeviceHelper(string serialPort, Lifetime lifetime, ILogger logger)
    {
        var deviceLifetime = lifetime.Intersect(solutionLifetime);
        
        lock (_devicesLock)
        {
            _devices.TryGetValue(serialPort)?.Dispose();
            
            var device = MeadowDeviceManager.GetMeadowForSerialPort(serialPort, false, logger).Result;
            if (device == null)
            {
                return null;
            }

            var helper = new MeadowDeviceHelper(device, logger);
            _devices[serialPort] = helper;

            deviceLifetime.OnTermination(() =>
            {
                lock (_devicesLock)
                {
                    if (_devices.TryGetValue(serialPort) != helper)
                        return;
                    helper.Dispose();
                    _devices.Remove(serialPort);
                }
            });

            return helper;
        }
    }

}