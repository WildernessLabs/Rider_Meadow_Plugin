package model.meadowPlugin

import com.jetbrains.rd.generator.nova.*
import com.jetbrains.rd.generator.nova.csharp.CSharp50Generator
import com.jetbrains.rd.generator.nova.kotlin.Kotlin11Generator
import com.jetbrains.rider.model.nova.ide.SolutionModel
import com.jetbrains.rider.model.nova.ide.rider.DeploymentHost.DeploymentArgsBase
import com.jetbrains.rider.model.nova.ide.rider.DeploymentHost.DeploymentResultBase

@Suppress("unused")
object MeadowPluginModel : Ext(SolutionModel.Solution) {

    private val MeadowDeploymentArgs = structdef extends DeploymentArgsBase {
        field("serialPort", PredefinedType.string)
        field("appPath", PredefinedType.string)
        field("debug", PredefinedType.bool)
    }

    private val MeadowDeploymentResult = structdef extends DeploymentResultBase {
    }

    private val DebugServerInfo = structdef {
        field("serialPort", PredefinedType.string)
        field("debugPort", PredefinedType.int)
    }

    init {
        call("getSerialPorts", PredefinedType.void, immutableList(PredefinedType.string)).async
        call("startDebugServer", DebugServerInfo, PredefinedType.void).async
        call("terminateTasks", PredefinedType.string, PredefinedType.void).async
        setting(Kotlin11Generator.Namespace, "com.jetbrains.rider.meadow.generated")
        setting(CSharp50Generator.Namespace, "Meadow.Generated")
    }
}