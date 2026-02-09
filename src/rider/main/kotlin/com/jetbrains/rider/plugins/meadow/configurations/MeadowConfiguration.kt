package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.intellij.platform.dap.DapLaunchArgumentsProvider
import com.intellij.platform.dap.DapStartRequest
import com.intellij.platform.dap.DebugAdapterId
import com.jetbrains.rider.plugins.meadow.dap.MeadowDebugAdapterId
import com.jetbrains.rider.plugins.meadow.devices.MeadowDevicesProvider
import com.jetbrains.rider.run.configurations.IProjectBasedRunConfiguration
import com.jetbrains.rider.run.configurations.RiderRunConfiguration
import com.intellij.internal.statistic.eventLog.events.EventPair
import com.jetbrains.rider.run.devices.DevicesConfiguration
import com.jetbrains.rider.run.devices.DevicesProvider
import org.jdom.Element
import java.io.File

class MeadowConfiguration(
    name: String,
    project: Project,
    factory: ConfigurationFactory,
    val parameters: MeadowConfigurationParameters
) : RiderRunConfiguration(name, project, factory, { MeadowConfigurationEditor(it) }, MeadowExecutorFactory(parameters)),
    DevicesConfiguration, IProjectBasedRunConfiguration,
    DapLaunchArgumentsProvider, RunConfigurationWithSuppressedDefaultRunAction {

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

    // Required by updated RiderRunConfiguration API (usage data collection)
    // Return empty list for now; extend later with real usage metrics.
    override fun getAdditionalUsageData(): List<EventPair<*>> = emptyList()

    // DapLaunchArgumentsProvider — tells DapProgramRunner how to launch the DAP adapter
    override val adapterId: DebugAdapterId = MeadowDebugAdapterId
    override val request: DapStartRequest = DapStartRequest.Launch

    override fun arguments(): Map<String, Any?> {
        val executable = parameters.toExecutable(project)

        // Generate MSBuild property file — adapter reads OutputPath + AssemblyName from this
        val outputDir = executable.appPath.parentFile.absolutePath
        val assemblyName = executable.appPath.nameWithoutExtension
        val tempFile = File.createTempFile("meadow_debug_", ".props")
        tempFile.writeText("OutputPath=${outputDir}${File.separator}\nAssemblyName=${assemblyName}\n")
        tempFile.deleteOnExit()

        val debugPort = MeadowDebugProfileState.getNextDebuggingPort()

        return mapOf(
            "projectPath" to executable.projectFilePath,
            "projectConfiguration" to "Debug",
            "serial" to executable.device.port,
            "debugPort" to debugPort,
            "msbuildPropertyFile" to tempFile.absolutePath
        )
    }
}