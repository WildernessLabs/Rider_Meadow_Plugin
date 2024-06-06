using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.InteropServices;
using System.Threading.Tasks;
using JetBrains.HabitatDetector;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Util;
using Meadow.CLI.Core.DeviceManagement;
using Meadow.CLI.Core.Devices;
using ILogger = Microsoft.Extensions.Logging.ILogger;

namespace MeadowPlugin;

[SolutionComponent]
public class MeadowDevices
{
    private readonly Lifetime _solutionLifetime;
    private readonly object _devicesLock = new();

    public MeadowDevices(Lifetime solutionLifetime)
    {
        _solutionLifetime = solutionLifetime;
        NativeLibrary.SetDllImportResolver(typeof(System.IO.Ports.SerialPort).Assembly, (name, assembly, _) =>
        {
            var assemblyLocation = assembly.Location;
            // ReSharper disable SwitchExpressionHandlesSomeKnownEnumValuesWithExceptionInDefault
            var nativeLibrary = Path.GetFullPath(HabitatInfo.Platform switch
            {
                JetPlatform.Linux => HabitatInfo.ProcessArchitecture switch
                {
                    JetArchitecture.Arm => Path.Combine(assemblyLocation, $"../../../../linux-arm/native/{name}.so"),
                    JetArchitecture.Arm64 => Path.Combine(assemblyLocation,
                        $"../../../../linux-arm64/native/{name}.so"),
                    JetArchitecture.X64 => Path.Combine(assemblyLocation, $"../../../../linux-x64/native/{name}.so"),
                    _ => throw new ArgumentOutOfRangeException(
                        $"Unsupported Linux processor architecture: {HabitatInfo.ProcessArchitecture}")
                },
                JetPlatform.MacOsX => HabitatInfo.ProcessArchitecture switch
                {
                    JetArchitecture.Arm64 => Path.Combine(assemblyLocation,
                        $"../../../../osx-arm64/native/{name}.dylib"),
                    JetArchitecture.X64 => Path.Combine(assemblyLocation, $"../../../../osx-x64/native/{name}.dylib"),
                    _ => throw new ArgumentOutOfRangeException(
                        $"Unsupported macOS processor architecture: {HabitatInfo.ProcessArchitecture}")
                },
                JetPlatform.Windows => HabitatInfo.ProcessArchitecture switch
                {
                    JetArchitecture.Arm64 => Path.Combine(assemblyLocation, $"../../../../win-arm64/native/{name}.dll"),
                    JetArchitecture.X64 => Path.Combine(assemblyLocation, $"../../../../win-x64/native/{name}.dll"),
                    JetArchitecture.X86 => Path.Combine(assemblyLocation, $"../../../../win-x86/native/{name}.dll"),
                    _ => throw new ArgumentOutOfRangeException(
                        $"Unsupported Windows processor architecture: {HabitatInfo.ProcessArchitecture}")
                },
                _ => throw new ArgumentOutOfRangeException(
                    $"Unsupported platform: {HabitatInfo.Platform}")
            });
            // ReSharper restore SwitchExpressionHandlesSomeKnownEnumValuesWithExceptionInDefault
            return Path.Exists(nativeLibrary) ? NativeLibrary.Load(nativeLibrary) : IntPtr.Zero;
        });
    }

    private readonly Dictionary<string, MeadowDeviceHelper> _devices = new();

    public async Task<MeadowDeviceHelper?> GetDeviceHelperAsync(string serialPort, Lifetime lifetime, ILogger logger)
    {
        var deviceLifetime = lifetime.Intersect(_solutionLifetime);

        lock (_devicesLock)
        {
            _devices.TryGetValue(serialPort)?.Dispose();
        }

        var device = await MeadowDeviceManager.GetMeadowForSerialPort(serialPort, false, logger);
        if (device == null)
        {
            return null;
        }

        var helper = new MeadowDeviceHelper(device, logger);
        lock (_devicesLock)
        {
            _devices[serialPort] = helper;
        }

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
