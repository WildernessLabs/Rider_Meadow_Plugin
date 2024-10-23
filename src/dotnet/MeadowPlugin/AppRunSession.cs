using System.Threading.Tasks;
using JetBrains.Lifetimes;
using Meadow.Hcom;
using MeadowPlugin.Model;

namespace MeadowPlugin;

internal class AppRunSession
{
    private IMeadowConnection? _meadowConnection;
    private readonly AppRunSessionModel? _model;
    private readonly LifetimeDefinition _lifetimeDefinition;

    public AppRunSession(string serialPort, IMeadowConnection meadowConnection, AppRunSessionModel model,
        LifetimeDefinition lifetimeDefinition)
    {
        SerialPort = serialPort;
        _meadowConnection = meadowConnection;
        _model = model;
        _lifetimeDefinition = lifetimeDefinition;

        if (_meadowConnection != null)
        {
            _meadowConnection.DeviceMessageReceived += MeadowConnection_DeviceMessageReceived;
        }

        _lifetimeDefinition.Lifetime.OnTermination(() =>
        {
            if (_meadowConnection != null)
            {
                _meadowConnection.DeviceMessageReceived -= MeadowConnection_DeviceMessageReceived;
            }
        });
    }

    public string SerialPort { get; }

    private void MeadowConnection_DeviceMessageReceived(object sender, (string message, string source) e)
    {
        if (_model != null)
        {
            _model.OutputReceived($"{e.source} {e.message}");
        }
    }

    public async Task TerminateAsync()
    {
        _lifetimeDefinition.Terminate();

        if (_meadowConnection != null)
        {
            await _meadowConnection.RuntimeDisable();

            _meadowConnection.Dispose();

            _meadowConnection = null;
        }
    }
}