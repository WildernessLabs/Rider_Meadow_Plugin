package com.jetbrains.rider.meadow.configurations

import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy
import com.intellij.openapi.project.Project
import com.jetbrains.rider.run.configurations.DotNetConfigurationFactoryBase

class MeadowConfigurationFactory(type: ConfigurationType) : DotNetConfigurationFactoryBase<MeadowConfiguration>(type) {
    override fun getId() = "Meadow.OS"

    override fun getSingletonPolicy(): RunConfigurationSingletonPolicy {
        return RunConfigurationSingletonPolicy.SINGLE_INSTANCE
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        MeadowConfiguration(
            "", project, this,
            MeadowConfigurationParameters("", project)
        )

}