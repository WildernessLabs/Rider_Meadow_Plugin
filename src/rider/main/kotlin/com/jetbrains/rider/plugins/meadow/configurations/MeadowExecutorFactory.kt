package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.jetbrains.rider.run.configurations.IExecutorFactory

class MeadowExecutorFactory(private val parameters: MeadowConfigurationParameters) : IExecutorFactory {
    override fun create(executorId: String, environment: ExecutionEnvironment): RunProfileState {
        // DAP handles both Run and Debug via the shared adapter (vscode-meadow.exe).
        // Return a no-op state — DapProcessStarter creates a stub ExecutionResult when null.
        return object : RunProfileState {
            override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult? = null
        }
    }
}