package com.jetbrains.rider.meadow.configurations

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.rider.meadow.devices.MeadowDevice
import com.jetbrains.rider.meadow.generated.meadowPluginModel
import com.jetbrains.rider.projectView.solution
import java.io.OutputStream

class MeadowDummyProcessHandler(private val meadowDevice: MeadowDevice, private val project: Project) : ProcessHandler() {
    override fun destroyProcessImpl() {
        project.solution.meadowPluginModel.resetDevice.start(project.lifetime, meadowDevice.port)
    }

    override fun detachProcessImpl() {
        destroyProcessImpl()
    }

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null
}