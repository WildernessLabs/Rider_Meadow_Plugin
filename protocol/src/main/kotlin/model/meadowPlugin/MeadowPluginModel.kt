package model.meadowPlugin

import com.jetbrains.rd.generator.nova.*
import com.jetbrains.rd.generator.nova.csharp.CSharp50Generator
import com.jetbrains.rd.generator.nova.kotlin.Kotlin11Generator
import com.jetbrains.rider.model.nova.ide.SolutionModel
import com.jetbrains.rider.model.nova.ide.rider.DeploymentHost.DeploymentArgsBase
import com.jetbrains.rider.model.nova.ide.rider.DeploymentHost.DeploymentResultBase

@Suppress("unused")
object MeadowPluginModel : Ext(SolutionModel.Solution) {

    private val CliRunnerInfoBase = basestruct {
        field("cliPath", PredefinedType.string)
    }

    private val CliRunnerInfo = structdef extends CliRunnerInfoBase {}

    private val CliRunnerInfoOnPort = structdef extends CliRunnerInfoBase {
        field("serialPort", PredefinedType.string)
    }

    private val MeadowDeploymentArgs = structdef extends DeploymentArgsBase {
        field("runnerInfo", CliRunnerInfoOnPort)
        field("appPath", PredefinedType.string)
        field("debug", PredefinedType.bool)
    }

    private val MeadowDeploymentResult = structdef extends DeploymentResultBase {
    }

    private val DebugServerInfo = structdef {
        field("runnerInfo", CliRunnerInfoOnPort)
        field("debugPort", PredefinedType.int)
    }

    init {
        call("getSerialPorts", CliRunnerInfo, immutableList(PredefinedType.string)).async
        call("startDebugServer", DebugServerInfo, PredefinedType.void).async
        call("terminate", CliRunnerInfoOnPort, PredefinedType.void).async
        setting(Kotlin11Generator.Namespace, "com.jetbrains.rider.plugins.meadow.model")
        setting(CSharp50Generator.Namespace, "MeadowPlugin.Model")
    }
}