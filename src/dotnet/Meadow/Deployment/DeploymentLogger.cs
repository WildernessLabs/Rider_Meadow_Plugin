extern alias MicrosoftLogger;
using System;
using JetBrains.DataFlow;
using JetBrains.Rider.Model;
using MicrosoftLogger::Microsoft.Extensions.Logging;

namespace Meadow.Deployment;

public class DeploymentLogger(DeploymentSession deploymentSession) : ILogger
{
    public void Log<TState>(LogLevel logLevel, EventId eventId, TState state, Exception? exception,
        Func<TState, Exception?, string> formatter)
    {
        switch (logLevel)
        {
            case LogLevel.Trace:
            case LogLevel.Debug:
            case LogLevel.Information:
                deploymentSession.OutputAdded(new OutputMessage(formatter(state, exception), DeployMessageKind.Info));
                break;
            case LogLevel.Warning:
                deploymentSession.OutputAdded(new OutputMessage(formatter(state, exception),
                    DeployMessageKind.Warning));
                break;
            case LogLevel.Error:
            case LogLevel.Critical:
                deploymentSession.OutputAdded(new OutputMessage(formatter(state, exception), DeployMessageKind.Error));
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