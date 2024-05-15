package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.rd.util.error
import com.jetbrains.rd.util.getLogger
import com.jetbrains.rd.util.threading.coroutines.launch
import com.jetbrains.rdclient.util.idea.pumpMessages
import com.jetbrains.rider.plugins.meadow.devices.MeadowDevice
import com.jetbrains.rider.plugins.meadow.model.meadowPluginModel
import com.jetbrains.rider.projectView.solution
import kotlinx.coroutines.Dispatchers
import java.io.OutputStream
import java.time.Duration

private val logger = getLogger<MeadowAppProcessHandler>()

private val MONO_RUNTIME_DISABLE_TIMEOUT = Duration.ofSeconds(10)

class MeadowAppProcessHandler(private val meadowDevice: MeadowDevice, private val project: Project) : ProcessHandler() {
    override fun destroyProcessImpl() {
        val job = project.lifetime.launch(Dispatchers.Default) {
            project.solution.meadowPluginModel.terminate.startSuspending(createRunnerInfoOnPort(meadowDevice))
        }

        pumpMessages(MONO_RUNTIME_DISABLE_TIMEOUT) { job.isCompleted }
        if (!job.isCompleted) {
            logger.error { "Mono runtime wasn't disabled in $MONO_RUNTIME_DISABLE_TIMEOUT" }
        }

        notifyProcessTerminated(0)
    }

    override fun detachProcessImpl() {
        destroyProcessImpl()
    }

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null //TODO
}