using System;
using System.Threading.Tasks;
using JetBrains.DataFlow;
using JetBrains.Rider.Model;
using Microsoft.Extensions.Logging;

namespace MeadowPlugin.Logging;

class DeploymentSessionLogger(DeploymentSession deploymentSession) : Microsoft.Extensions.Logging.ILogger
{
    string previousFileName = string.Empty;
    uint previousPercentage = 0;

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

    internal async Task ReportDeviceMessage(string source, string message)
    {
        this.LogInformation($"{source}: {message}");
    }

    internal async Task ReportFileProgress(string fileName, uint percentage)
    {
        if (percentage > 0
        && percentage > 99)
        {
            if (!previousFileName.Equals(fileName)
            || !previousPercentage.Equals(percentage))
            {
                this.LogInformation($"{percentage}% of '{fileName}' Sent");
                previousFileName = fileName;
                previousPercentage = percentage;
            }
        }
    }
}