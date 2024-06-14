package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rdclient.util.idea.pumpMessages
import com.jetbrains.rider.deploy.RiderDeploymentHost
import com.jetbrains.rider.plugins.meadow.devices.MeadowDevice
import com.jetbrains.rider.plugins.meadow.model.DeviceModel
import com.jetbrains.rider.plugins.meadow.model.MeadowDeploymentArgs
import com.jetbrains.rider.plugins.meadow.model.MeadowDeploymentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async

@OptIn(ExperimentalCoroutinesApi::class)
fun deployWithoutDebugging(executable: MeadowExecutable, project: Project) : MeadowDeploymentResult {
    val deploymentResultDeferred = project.service<MeadowLifetimeService>().scope.async(Dispatchers.Main) {
        deploy(executable, -1, project)
    }

    pumpMessages { deploymentResultDeferred.isCompleted }

    return deploymentResultDeferred.getCompleted()
}

suspend fun deploy(executable: MeadowExecutable, debugPort: Int, project: Project): MeadowDeploymentResult {
    return RiderDeploymentHost.getInstance(project).deployWithProgress(
        MeadowDeploymentArgs(
            executable.device.toModel(),
            executable.appPath.absolutePath,
            debugPort,
            executable.runnableProject.kind,
            executable.projectFilePath)
    )
}

fun MeadowDevice.toModel() : DeviceModel {
    return DeviceModel(port)
}