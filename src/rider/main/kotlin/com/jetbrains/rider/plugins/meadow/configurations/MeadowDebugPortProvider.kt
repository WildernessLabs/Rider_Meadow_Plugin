package com.jetbrains.rider.plugins.meadow.configurations

/**
 * Provides unique debug port numbers for Meadow debugging sessions.
 * Centralized from deleted MeadowDebugProfileState.
 */
object MeadowDebugPortProvider {
    private const val BASE_PORT = 55898
    private var nextPortCounter = 0

    fun getNextDebuggingPort(): Int {
        val shift = nextPortCounter++
        if (nextPortCounter > 100) {
            nextPortCounter = 0
        }
        return BASE_PORT + shift
    }
}
