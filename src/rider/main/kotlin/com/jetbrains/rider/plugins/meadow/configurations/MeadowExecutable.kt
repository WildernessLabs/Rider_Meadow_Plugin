package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.execution.CantRunException
import com.intellij.openapi.project.Project
import com.jetbrains.rider.plugins.meadow.devices.MeadowDevice
import com.jetbrains.rider.plugins.meadow.messages.MeadowBundle
import com.jetbrains.rider.model.RunnableProject
import com.jetbrains.rider.model.runnableProjectsModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.run.RiderRunBundle
import com.jetbrains.rider.run.devices.ActiveDeviceManager
import java.io.File

data class MeadowExecutable(
    val runnableProject: RunnableProject,
    val projectFilePath: String,
    val appPath: File,
    val device: MeadowDevice
)

fun MeadowConfigurationParameters.toExecutable(project: Project) : MeadowExecutable {
    val device = ActiveDeviceManager.getInstance(project).getDevice<MeadowDevice>()
        ?: throw CantRunException(MeadowBundle.message("meadow.os.device.not.selected.message"))
    val runnableProject = tryGetRunnableProject(projectFilePath, project)
        ?: throw CantRunException(RiderRunBundle.message("dialog.message.not.specified.project.error"))
    val appPath = runnableProject.projectOutputs.firstNotNullOfOrNull {
        val appPath = File(it.exePath)
        if (appPath.exists()) appPath else null
    } ?: throw CantRunException(MeadowBundle.message("meadow.app.does.not.exist.message"))

    return MeadowExecutable(runnableProject, projectFilePath, appPath, device)
}

private fun tryGetRunnableProject(projectFilePath: String, project: Project): RunnableProject? {
    val runnableProjects = project.solution.runnableProjectsModel.projects.valueOrNull
    if (runnableProjects != null) {
        return runnableProjects.singleOrNull {
            it.projectFilePath == projectFilePath && isTypeApplicable(it.kind)
        }
    }
    return null
}