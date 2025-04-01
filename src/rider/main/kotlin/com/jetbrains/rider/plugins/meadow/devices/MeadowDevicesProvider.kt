package com.jetbrains.rider.plugins.meadow.devices

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.rider.plugins.meadow.icons.Icons
import com.jetbrains.rider.plugins.meadow.messages.MeadowBundle
import com.jetbrains.rider.plugins.meadow.model.meadowPluginModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.run.devices.*

class MeadowDevicesProvider(private val project: Project) : DevicesProvider {
    override fun checkCompatibility(device: Device): CompatibilityProblem? {
        return checkCompatibility(device.kind)
    }

    override fun checkCompatibility(deviceKind: DeviceKind): CompatibilityProblem? {
        if (deviceKind != MeadowDeviceKind) {
            return CompatibilityProblem(MeadowBundle.message("meadow.compatibility.problem.message", deviceKind.name))
        }
        return null
    }

    override fun getDeviceKinds(): List<DeviceKind> = listOf(MeadowDeviceKind)

    override suspend fun loadAllDevices(): List<Device> {
        return project.solution.meadowPluginModel.getSerialPorts.startSuspending(project.lifetime, Unit).map { MeadowDevice(it) }
    }
}

object MeadowDeviceKind : DeviceKind(MeadowBundle.message("meadow.os.message"), MeadowBundle.message("meadow.os.category.message")) {
    override fun getMissingDevicesAction(): AnAction =  NoDeviceAction(MeadowBundle.message("meadow.missing.device.message"), AllIcons.General.Warning)
}

data class MeadowDevice(val port: String) : Device(port, Icons.Main, MeadowDeviceKind)