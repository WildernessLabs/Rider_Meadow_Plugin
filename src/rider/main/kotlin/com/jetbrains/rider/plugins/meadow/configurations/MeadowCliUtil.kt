package com.jetbrains.rider.plugins.meadow.configurations

import com.jetbrains.rider.CPUKind
import com.jetbrains.rider.RiderEnvironment
import com.jetbrains.rider.plugins.meadow.devices.MeadowDevice
import com.jetbrains.rider.plugins.meadow.model.CliRunnerInfo
import com.jetbrains.rider.plugins.meadow.model.CliRunnerInfoOnPort

fun createRunnerInfoOnPort(device: MeadowDevice) : CliRunnerInfoOnPort {
    return CliRunnerInfoOnPort(device.port, RiderEnvironment.getBundledFile(CPUKind.RIDER_CPU_KIND.getDotnetExecutable()).absolutePath)
}

fun createRunnerInfo(): CliRunnerInfo {
    return CliRunnerInfo(RiderEnvironment.getBundledFile(CPUKind.RIDER_CPU_KIND.getDotnetExecutable()).absolutePath)
}