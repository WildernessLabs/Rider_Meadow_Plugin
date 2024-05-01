package com.jetbrains.rider.meadow.devices

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.rider.meadow.generated.meadowPluginModel
import com.jetbrains.rider.meadow.icons.Icons
import com.jetbrains.rider.meadow.messages.MeadowBundle
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.run.devices.CompatibilityProblem
import com.jetbrains.rider.run.devices.Device
import com.jetbrains.rider.run.devices.DeviceKind
import com.jetbrains.rider.run.devices.DevicesProvider
import javax.swing.Icon

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
    override fun getMissingIcon(): Icon = AllIcons.General.Warning

    override fun getMissingMessage(): String = MeadowBundle.message("meadow.missing.device.message")
}

data class MeadowDevice(val port: String) : Device(port, Icons.Main, MeadowDeviceKind)