package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.rd.util.lifetime.LifetimeDefinition
import com.jetbrains.rd.util.threading.coroutines.launch
import com.jetbrains.rider.plugins.meadow.devices.MeadowDevice
import com.jetbrains.rider.plugins.meadow.model.meadowPluginModel
import com.jetbrains.rider.projectView.solution
import kotlinx.coroutines.Dispatchers
import java.io.OutputStream

class MeadowAppProcessHandler(private val meadowDevice: MeadowDevice, private val project: Project) : ProcessHandler() {
    private val lifetimeDef: LifetimeDefinition = project.lifetime.createNested()

    init {
        project.solution.meadowPluginModel.appOutput.advise(lifetimeDef.lifetime) {
            if (it.serialPort == meadowDevice.port) {
                notifyTextAvailable(it.text, ProcessOutputTypes.STDOUT)
            }
        }
    }

    override fun destroyProcessImpl() {
        lifetimeDef.terminate()
        project.lifetime.launch(Dispatchers.Default) {
            project.solution.meadowPluginModel.terminate.startSuspending(meadowDevice.toModel())
        }
        notifyProcessTerminated(0)
    }

    override fun detachProcessImpl() {
        lifetimeDef.terminate()
        notifyProcessDetached()
    }

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null
}