package com.jetbrains.rider.meadow.configurations

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class MeadowLifetimeService(private val project: Project, val scope: CoroutineScope)