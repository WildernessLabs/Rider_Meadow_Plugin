package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.rider.plugins.meadow.devices.MeadowDevice
import com.jetbrains.rider.plugins.meadow.model.meadowPluginModel
import com.jetbrains.rider.projectView.solution
import java.io.OutputStream

class MeadowAppProcessHandler(private val meadowDevice: MeadowDevice, private val project: Project) : ProcessHandler() {
    override fun destroyProcessImpl() {
        project.solution.meadowPluginModel.terminate.start(project.lifetime, meadowDevice.port)
        notifyProcessTerminated(0)
    }

    override fun detachProcessImpl() {
        destroyProcessImpl()
    }

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null //TODO
}