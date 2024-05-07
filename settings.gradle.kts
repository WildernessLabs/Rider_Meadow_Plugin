pluginManagement {
    repositories {
        maven { setUrl("https://cache-redirector.jetbrains.com/plugins.gradle.org") }
    }
    resolutionStrategy {
        eachPlugin {
            when(requested.id.name) {
                "rdgen" -> {
                    useModule("com.jetbrains.rd:rd-gen:${requested.version}")
                }
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "meadow-plugin"

include(":protocol")
