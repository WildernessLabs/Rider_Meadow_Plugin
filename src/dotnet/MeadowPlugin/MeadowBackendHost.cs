using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Ports;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Threading.Tasks;
using JetBrains.Application.Parts;
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
using MeadowPlugin.Logging;
using MeadowPlugin.Model;
using Microsoft.Extensions.Logging;
using ILogger = JetBrains.Util.ILogger;

namespace MeadowPlugin;

[SolutionComponent(Instantiation.DemandAnyThreadUnsafe)]
public class MeadowBackendHost
{
    private static readonly ILogger OurLogger = Logger.GetLogger<MeadowBackendHost>();

    private readonly Lifetime _solutionLifetime;
    private readonly MeadowPluginModel _meadowPluginModel;

    public MeadowPluginModel Model => _meadowPluginModel;

    // AppRunSession removed — all device output handled by DAP adapter

    IMeadowConnection? _meadowConnection;
    private MeadowActionsLogger _meadowActionsLogger;

    static MeadowBackendHost()
    {
        if (OperatingSystem.IsLinux() || OperatingSystem.IsMacOS())
        {
            NativeLibrary.SetDllImportResolver(typeof(SerialPort).Assembly,
                (libraryName, _, _) =>
                {
                    var probe = Path.Combine(
                        Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location)!,
                        "runtimes",
                        $"{(OperatingSystem.IsLinux() ? "linux" : "osx")}-{RuntimeInformation.ProcessArchitecture.ToString().ToLowerInvariant()}",
                        "native",
                        $"{libraryName}{(OperatingSystem.IsLinux() ? ".so" : ".dylib")}"
                    );
                    return File.Exists(probe) ? NativeLibrary.Load(probe) : nint.Zero;
                });
        }
    }

    public MeadowBackendHost(ISolution solution, Lifetime solutionLifetime)
    {
        _solutionLifetime = solutionLifetime;
        _meadowPluginModel = solution.GetProtocolSolution().GetMeadowPluginModel();
        _meadowPluginModel.GetSerialPorts.SetAsync(GetSerialPortsAsync);
        // DropSessionForPort: no-op since AppRunSession was removed (all output via DAP)
        _meadowPluginModel.DropSessionForPort.SetAsync(async (lifetime, port) =>
        {
            return Unit.Instance;
        });

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

    public async Task RegisterAppSessionAsync(string serialPort, int debugPort, IMeadowConnection meadowConnection)
    {
        _meadowConnection = meadowConnection;

        if (_meadowConnection != null)
        {
            var isDebugging = debugPort > 0;

            // DAP adapter handles all device output — AppRunSession removed for centralization
            if (isDebugging)
            {
                _meadowActionsLogger.LogInformation("Debugging application...");
                var sessionLifetime = _solutionLifetime.CreateNested();
                await _meadowConnection.StartDebuggingSession(debugPort, _meadowActionsLogger,
                    sessionLifetime.Lifetime, "Rider");
            }
        }
    }
}