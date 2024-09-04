using System;
using System.Threading.Tasks;
using JetBrains.DataFlow;
using JetBrains.Util;
using JetBrains.Util.Logging;
using Microsoft.Extensions.Logging;
using ILogger = JetBrains.Util.ILogger;

namespace MeadowPlugin.Logging;

internal class MeadowActionsLogger : Microsoft.Extensions.Logging.ILogger
{
    private static readonly ILogger OurLogger = Logger.GetLogger<MeadowActionsLogger>();

    public void Log<TState>(LogLevel logLevel, EventId eventId, TState state, Exception? exception,
        Func<TState, Exception?, string> formatter)
    {
        switch (logLevel)
        {
            case LogLevel.Trace:
            case LogLevel.Debug:
                OurLogger.Trace(formatter(state, exception));
                break;
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