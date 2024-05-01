package com.jetbrains.rider.meadow.configurations

import com.intellij.execution.CantRunException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rider.debugger.DebuggerWorkerProcessHandler
import com.jetbrains.rider.meadow.messages.MeadowBundle
import com.jetbrains.rider.model.DeploymentResultStatus
import com.jetbrains.rider.model.debuggerWorker.DebuggerStartInfoBase
import com.jetbrains.rider.run.ConsoleKind
import com.jetbrains.rider.run.DebugProfileStateBase
import com.jetbrains.rider.run.configurations.remote.DotNetRemoteConfiguration
import com.jetbrains.rider.run.configurations.remote.MonoConnectRemoteProfileState
import com.jetbrains.rider.run.configurations.remote.MonoRemoteConfigType

class MeadowDebugProfileState(private val executable: MeadowExecutable, private val environment: ExecutionEnvironment) :
    DebugProfileStateBase(environment) {
    override val attached: Boolean = false
    override val consoleKind: ConsoleKind = ConsoleKind.Normal

    private val attachConfiguration = DotNetRemoteConfiguration(
        environment.project,
        ConfigurationTypeUtil.findConfigurationType(MonoRemoteConfigType::class.java).factory,
        executable.runnableProject.name
    )
    private val attachState = MonoConnectRemoteProfileState(
        attachConfiguration,
        environment)

    override suspend fun createModelStartInfo(lifetime: Lifetime): DebuggerStartInfoBase {
        return attachState.createModelStartInfo(lifetime)
    }

    override suspend fun execute(
        executor: Executor,
        runner: ProgramRunner<*>,
        workerProcessHandler: DebuggerWorkerProcessHandler
    ): ExecutionResult {
        val deploymentResult = deploy(executable, true, environment.project)
        if (deploymentResult.status != DeploymentResultStatus.Success) {
            throw CantRunException(MeadowBundle.message("meadow.deployment.failed.message"))
        }
        attachConfiguration.port = deploymentResult.debugPort
        return attachState.execute(executor, runner, workerProcessHandler)
    }
}