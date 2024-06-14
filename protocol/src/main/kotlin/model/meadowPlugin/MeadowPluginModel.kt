package model.meadowPlugin

import com.jetbrains.rd.generator.nova.*
import com.jetbrains.rd.generator.nova.csharp.CSharp50Generator
import com.jetbrains.rd.generator.nova.kotlin.Kotlin11Generator
import com.jetbrains.rider.model.nova.ide.SolutionModel
import com.jetbrains.rider.model.nova.ide.rider.DeploymentHost.DeploymentArgsBase
import com.jetbrains.rider.model.nova.ide.rider.DeploymentHost.DeploymentResultBase

@Suppress("unused")
object MeadowPluginModel : Ext(SolutionModel.Solution) {

    private val DeviceModel = structdef {
        field("serialPort", PredefinedType.string)
    }

    private val MeadowDeploymentArgs = structdef extends DeploymentArgsBase {
        field("device", DeviceModel)
        field("appPath", PredefinedType.string)
        field("debugPort", PredefinedType.int)
    }

    private val MeadowDeploymentResult = structdef extends DeploymentResultBase {
    }

    private val AppRunSessionModel = classdef {
        sink("outputReceived", PredefinedType.string).async
        source("terminate", PredefinedType.void).async
    }

    private val AppOutput = structdef {
        field("serialPort", PredefinedType.string)
        field("text", PredefinedType.string)
    }

    init {
        call("getSerialPorts", PredefinedType.void, immutableList(PredefinedType.string)).async
        map("runSessions", PredefinedType.string, AppRunSessionModel).async
        sink("appOutput", AppOutput)
        setting(Kotlin11Generator.Namespace, "com.jetbrains.rider.plugins.meadow.model")
        setting(CSharp50Generator.Namespace, "MeadowPlugin.Model")
    }
}