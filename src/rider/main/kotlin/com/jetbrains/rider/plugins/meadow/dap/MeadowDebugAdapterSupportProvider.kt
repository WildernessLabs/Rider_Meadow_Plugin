package com.jetbrains.rider.plugins.meadow.dap

import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.lifetime
import com.intellij.platform.dap.DapBreakpointsDescription
import com.intellij.platform.dap.DebugAdapterDescriptor
import com.intellij.platform.dap.DebugAdapterSupportProvider
import com.intellij.platform.dap.connection.CommandLineDebugAdapterHandle
import com.intellij.platform.dap.connection.DebugAdapterHandle
import com.jetbrains.rider.plugins.meadow.configurations.MeadowConfiguration
import com.jetbrains.rider.plugins.meadow.configurations.toExecutable
import com.jetbrains.rider.plugins.meadow.model.meadowPluginModel
import com.jetbrains.rider.projectView.solution

class MeadowDebugAdapterSupportProvider : DebugAdapterSupportProvider<MeadowDebugAdapterId> {
    override val adapterId = MeadowDebugAdapterId

    override fun createDebugAdapterDescriptor(project: Project): DebugAdapterDescriptor<MeadowDebugAdapterId> =
        object : DebugAdapterDescriptor<MeadowDebugAdapterId>() {
            override val id = MeadowDebugAdapterId

            override suspend fun launchDebugAdapter(
                environment: ExecutionEnvironment,
                executionResult: ExecutionResult?,
                sessionId: String,
            ): DebugAdapterHandle {
                // Release serial port before adapter takes exclusive access
                val config = environment.runnerAndConfigurationSettings?.configuration as? MeadowConfiguration
                if (config != null) {
                    val executable = config.parameters.toExecutable(project)
                    try {
                        project.solution.meadowPluginModel.dropSessionForPort
                            .startSuspending(project.lifetime, executable.device.port)
                    } catch (_: Exception) {
                        // No existing session to drop — that's fine
                    }
                }

                // Resolve adapter binary path within plugin distribution
                val pluginPath = PluginManagerCore.getPlugin(
                    PluginId.getId("com.wildernesslabs.rider.meadow")
                )!!.pluginPath
                val adapterExe = pluginPath.resolve("DapAdapter/meadow-debugging.exe")

                val commandLine = GeneralCommandLine(adapterExe.toString())
                
                // TODO: Add custom DAP event interception for progress reporting
                // For now, use standard handle - IntelliJ Platform auto-handles DAP protocol
                return CommandLineDebugAdapterHandle(commandLine)
            }

            override val breakpointsDescription = DapBreakpointsDescription(
                MeadowLineBreakpointType::class.java,
                MeadowExceptionBreakpointType::class.java
            )
        }
}
