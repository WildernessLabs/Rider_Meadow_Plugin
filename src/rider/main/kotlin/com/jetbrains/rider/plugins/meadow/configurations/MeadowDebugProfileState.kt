package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.execution.CantRunException
import com.intellij.execution.runners.ExecutionEnvironment
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rider.plugins.meadow.messages.MeadowBundle
import com.jetbrains.rider.model.DeploymentResultStatus
import com.jetbrains.rider.model.debuggerWorker.DebuggerStartInfoBase
import com.jetbrains.rider.model.debuggerWorker.MonoAttachStartInfo
import com.jetbrains.rider.plugins.meadow.model.DebugServerInfo
import com.jetbrains.rider.plugins.meadow.model.meadowPluginModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.run.AttachDebugProfileStateBase
import com.jetbrains.rider.run.ConsoleKind

class MeadowDebugProfileState(private val executable: MeadowExecutable, private val environment: ExecutionEnvironment) :
    AttachDebugProfileStateBase(environment) {

    companion object {
        private const val BASE_PORT = 55898

        private var nextPortCounter = 0

        fun getNextDebuggingPort() : Int {
            val shift = nextPortCounter++
            if (nextPortCounter > 100)
            {
                nextPortCounter = 0
            }

            return BASE_PORT + shift
        }
    }

    override val attached: Boolean = false
    override val consoleKind: ConsoleKind = ConsoleKind.Normal


    override suspend fun createModelStartInfo(lifetime: Lifetime): DebuggerStartInfoBase {
        val deploymentResult = deploy(executable, true, environment.project)
        if (deploymentResult.status != DeploymentResultStatus.Success) {
            throw CantRunException(MeadowBundle.message("meadow.deployment.failed.message"))
        }

        val debugPort = getNextDebuggingPort()
        environment.project.solution.meadowPluginModel.startDebugServer.startSuspending(
            lifetime,
            DebugServerInfo(executable.device.toModel(), debugPort)
        )

        return MonoAttachStartInfo("localhost", debugPort, false)
    }
}