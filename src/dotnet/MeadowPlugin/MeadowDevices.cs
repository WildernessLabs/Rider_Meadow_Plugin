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

    private readonly ILogger _devicesLogger = new MeadowActionsLogger();

    public MeadowDeviceHelper? GetDeviceHelper(string serialPort, Lifetime lifetime)
    {
        var deviceLifetime = lifetime.Intersect(solutionLifetime);
        
        lock (_devicesLock)
        {
            _devices.TryGetValue(serialPort)?.Dispose();
            var device = MeadowDeviceManager.FindMeadowBySerialNumber(serialPort, _devicesLogger, 10, deviceLifetime).Result;
            if (device == null)
            {
                return null;
            }

            var helper = new MeadowDeviceHelper(device, _devicesLogger);
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