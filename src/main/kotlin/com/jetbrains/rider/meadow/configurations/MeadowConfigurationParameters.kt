package com.jetbrains.rider.meadow.configurations

import com.intellij.openapi.project.Project

data class MeadowConfigurationParameters(var projectFilePath: String, val project: Project) {

}