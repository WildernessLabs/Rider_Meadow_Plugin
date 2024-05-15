using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Threading.Tasks;
using JetBrains.Application.Threading;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.Threading;
using JetBrains.Util;
using JetBrains.Util.Logging;

namespace MeadowPlugin;

[SolutionComponent]
public class MeadowCliExecutor(IShellLocks locks)
{
    private static readonly ILogger OurLogger = Logger.GetLogger<MeadowCliExecutor>();

    private readonly object _taskCreationLock = new();
    private readonly Dictionary<string, LifetimeDefinition> _serialPortTaskLifetimes = new();

    public Task<int> ExecuteMeadowCommandForSerialPort(
        string serialPort,
        string[] command,
        Lifetime lifetime,
        Action<string>? outputConsumer = null,
        Action<string>? errorConsumer = null)
    {
        lock (_taskCreationLock)
        {
            _serialPortTaskLifetimes.TryGetValue(serialPort)?.Terminate();
            var meadowTask = ExecuteMeadowCommandInternal(
                command.Append("--SerialPort").Append(serialPort).ToArray(),
                lifetime,
                outputConsumer,
                errorConsumer);
            _serialPortTaskLifetimes[serialPort] = meadowTask.Item2;
            return meadowTask.Item1;
        }
    }

    public Task<int> ExecuteMeadowCommand(
        string[] command,
        Lifetime lifetime,
        Action<string>? outputConsumer = null,
        Action<string>? errorConsumer = null)
    {
        return ExecuteMeadowCommandInternal(command, lifetime, outputConsumer, errorConsumer).Item1;
    }

    private Tuple<Task<int>, LifetimeDefinition> ExecuteMeadowCommandInternal(
        string[] command,
        Lifetime lifetime,
        Action<string>? outputConsumer,
        Action<string>? errorConsumer)
    {
        var processLaunchLifetimeDef = lifetime.CreateNested();
        var processLaunchLifetime = processLaunchLifetimeDef.Lifetime;
        var task = ExecuteMeadowCommand(command, processLaunchLifetime, processLaunchLifetimeDef, outputConsumer,
            errorConsumer);
        return Tuple.Create(task, processLaunchLifetimeDef);
    }

    private async Task<int> ExecuteMeadowCommand(
        string[] command,
        Lifetime lifetime,
        LifetimeDefinition lifetimeDefinition,
        Action<string>? outputConsumer,
        Action<string>? errorConsumer)
    {
        try
        {
            var assemblyPath = FileSystemPath.TryParse(Assembly.GetExecutingAssembly().Location);
            if (!assemblyPath.IsValidAndExistFile())
            {
                OurLogger.Error("Unable to find plugin root directory");
                return 1;
            }

            var workingDirectory = assemblyPath.Parent.Parent.GetChildDirectories("Meadow.CLI").FirstOrDefault();
            if (workingDirectory == null)
            {
                OurLogger.Error("Unable to find Meadow.CLI directory");
                return 1;
            }

            var startInfo = new ProcessStartInfo("C:/Program Files/dotnet/dotnet.exe", //TODO
                command.Prepend("--roll-forward", "LatestMajor",
                    CommandLineUtil.QuoteIfNeeded($"{workingDirectory}/meadow.dll")))
            {
                WorkingDirectory = workingDirectory.FullPath,
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false
            };
            var process = Process.Start(startInfo);

            if (process == null)
            {
                OurLogger.Error($"Process for command line [{string.Join(", ", command)}] is not expected to be null");
                return 1;
            }

            lifetime.OnTermination(() => { process.Kill(); });


            locks.StartBackground(lifetime, async () =>
            {
                while (true)
                {
                    var nextOutput = await process.StandardOutput.ReadLineAsync(lifetime);
                    if (nextOutput == null) break;
                    outputConsumer?.Invoke(nextOutput);
                }
            }).NoAwait();

            locks.StartBackground(lifetime, async () =>
            {
                while (true)
                {
                    var nextError = await process.StandardError.ReadLineAsync(lifetime);
                    if (nextError == null) break;
                    errorConsumer?.Invoke(nextError);
                }
            }).NoAwait();

            await process.WaitForExitAsync(lifetime);
            return process.ExitCode;
        }
        finally
        {
            lifetimeDefinition.Terminate();
        }
    }
}