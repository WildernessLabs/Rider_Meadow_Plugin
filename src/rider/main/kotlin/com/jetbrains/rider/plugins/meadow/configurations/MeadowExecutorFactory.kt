package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.execution.CantRunException
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.jetbrains.rider.run.RiderRunBundle
import com.jetbrains.rider.run.configurations.IExecutorFactory

class MeadowExecutorFactory(private val parameters: MeadowConfigurationParameters) : IExecutorFactory {
    override fun create(executorId: String, environment: ExecutionEnvironment): RunProfileState {
        return when (executorId) {
            DefaultDebugExecutor.EXECUTOR_ID ->
                MeadowDebugProfileState(parameters.toExecutable(environment.project), environment)

            DefaultRunExecutor.EXECUTOR_ID ->
                MeadowRunProfileState(parameters.toExecutable(environment.project), environment)

            else -> throw CantRunException(
                RiderRunBundle.message(
                    "dialog.message.unsupported.executor.error",
                    executorId
                )
            )
        }
    }
}