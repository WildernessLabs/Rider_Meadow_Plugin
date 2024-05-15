package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.execution.CantRunException
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.jetbrains.rider.plugins.meadow.messages.MeadowBundle
import com.jetbrains.rider.model.DeploymentResultStatus
import com.jetbrains.rider.run.IDotNetProfileState

class MeadowRunProfileState(private val executable: MeadowExecutable, private val environment: ExecutionEnvironment) :
    IDotNetProfileState {
    override fun execute(p0: Executor?, p1: ProgramRunner<*>): ExecutionResult {
        val deploymentResult = deploySync(executable, false, environment.project)
        if (deploymentResult.status != DeploymentResultStatus.Success) {
            throw CantRunException(MeadowBundle.message("meadow.deployment.failed.message"))
        }

        return DefaultExecutionResult(MeadowAppProcessHandler(executable.device, environment.project))
    }
}