package com.jetbrains.rider.plugins.meadow.configurations

import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class MeadowLifetimeService(val scope: CoroutineScope)