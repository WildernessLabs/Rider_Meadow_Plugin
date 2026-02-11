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

    private val ProgressUpdate = structdef {
        field("fileName", PredefinedType.string)
        field("percentage", PredefinedType.int)
        field("status", PredefinedType.string)
    }

    init {
        call("getSerialPorts", PredefinedType.void, immutableList(PredefinedType.string)).async
        call("dropSessionForPort", PredefinedType.string, PredefinedType.void).async
        signal("progressUpdate", ProgressUpdate)
        // runSessions + appOutput removed — all device output handled by DAP adapter
        setting(Kotlin11Generator.Namespace, "com.jetbrains.rider.plugins.meadow.model")
        setting(CSharp50Generator.Namespace, "MeadowPlugin.Model")
    }
}