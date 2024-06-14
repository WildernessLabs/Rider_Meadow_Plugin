package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.execution.CantRunException
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.jetbrains.rider.model.DeploymentResultStatus
import com.jetbrains.rider.plugins.meadow.messages.MeadowBundle
import com.jetbrains.rider.plugins.meadow.model.meadowPluginModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.run.ConsoleKind
import com.jetbrains.rider.run.IDotNetProfileState
import com.jetbrains.rider.run.createConsole

class MeadowRunProfileState(private val executable: MeadowExecutable, private val environment: ExecutionEnvironment) :
    IDotNetProfileState {
    override fun execute(p0: Executor?, p1: ProgramRunner<*>): ExecutionResult {
        val deploymentResult = deployWithoutDebugging(executable, environment.project)
        if (deploymentResult.status != DeploymentResultStatus.Success) {
            throw CantRunException(MeadowBundle.message("meadow.deployment.failed.message"))
        }
        val appSession = environment.project.solution.meadowPluginModel.runSessions[executable.device.port]
            ?: throw IllegalStateException("Run model should not be null")

        val meadowAppProcessHandler = MeadowAppProcessHandler(appSession, environment.project)
        val console = createConsole(
            ConsoleKind.Normal,
            meadowAppProcessHandler,
            environment.project
        )
        return DefaultExecutionResult(console, meadowAppProcessHandler)
    }
}