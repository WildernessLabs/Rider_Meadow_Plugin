package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.rd.util.lifetime.LifetimeDefinition
import com.jetbrains.rider.plugins.meadow.model.AppRunSessionModel
import java.io.OutputStream

class MeadowAppProcessHandler(private val sessionModel: AppRunSessionModel, project: Project) : ProcessHandler() {
    private val lifetimeDef: LifetimeDefinition = project.lifetime.createNested()

    init {
        sessionModel.outputReceived.advise(lifetimeDef) {
            notifyTextAvailable(it, ProcessOutputTypes.STDOUT)
        }
    }

    override fun destroyProcessImpl() {
        lifetimeDef.terminate()
        sessionModel.terminate.fire(Unit)
        notifyProcessTerminated(0)
    }

    override fun detachProcessImpl() {
        lifetimeDef.terminate()
        notifyProcessDetached()
    }

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null
}