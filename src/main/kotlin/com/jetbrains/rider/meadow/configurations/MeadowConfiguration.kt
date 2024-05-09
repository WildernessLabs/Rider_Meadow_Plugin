package com.jetbrains.rider.meadow.configurations

import com.intellij.execution.BeforeRunTask
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.jetbrains.rider.debugger.IRiderDebuggable
import com.jetbrains.rider.meadow.devices.MeadowDevicesProvider
import com.jetbrains.rider.run.configurations.IProjectBasedRunConfiguration
import com.jetbrains.rider.run.configurations.RiderRunConfiguration
import com.jetbrains.rider.run.devices.DevicesConfiguration
import com.jetbrains.rider.run.devices.DevicesProvider
import org.jdom.Element

class MeadowConfiguration(
    name: String,
    project: Project,
    factory: ConfigurationFactory,
    val parameters: MeadowConfigurationParameters
) : RiderRunConfiguration(name, project, factory, { MeadowConfigurationEditor(it) }, MeadowExecutorFactory(parameters)),
    DevicesConfiguration, IRiderDebuggable, IProjectBasedRunConfiguration {

    companion object {
        private const val PROJECT_PATH = "PROJECT_PATH"
    }

    override val provider: DevicesProvider = MeadowDevicesProvider(project)

    override fun readExternal(element: Element) {
        parameters.projectFilePath = JDOMExternalizerUtil.readField(element, PROJECT_PATH) ?: ""
    }

    override fun writeExternal(element: Element) {
        JDOMExternalizerUtil.writeField(element, PROJECT_PATH, parameters.projectFilePath)
    }

    override fun getProjectFilePath(): String {
        return parameters.projectFilePath
    }

    override fun setProjectFilePath(path: String) {
        parameters.projectFilePath = path
    }
}