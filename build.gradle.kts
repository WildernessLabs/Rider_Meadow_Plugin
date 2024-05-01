import com.jetbrains.plugin.structure.base.utils.isFile
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.Constants
import kotlin.io.path.isDirectory

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.gradleIntelliJPlatformPlugin) // Gradle IntelliJ Platform Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    alias(libs.plugins.rdgen)
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

val riderModel: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

// Configure project's dependencies
repositories {
    mavenCentral()
    maven { setUrl("https://cache-redirector.jetbrains.com/maven-central") }
    intellijPlatform {
        defaultRepositories()
    }
}


dependencies {
    intellijPlatform {
        rider(properties("platformVersion"))
        instrumentationTools()
    }
}

artifacts {
    add(riderModel.name, provider {
        val sdkRoot = intellijPlatform.platformPath
        sdkRoot.resolve("lib/rd/rider-model.jar").also {
            check(it.isFile) {
                "rider-model.jar is not found at $riderModel"
            }
        }
    }) {
        builtBy(Constants.Tasks.INITIALIZE_INTELLIJ_PLATFORM_PLUGIN)
    }
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
//    implementation(libs.annotations)
}

intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
    }
    buildSearchableOptions = false
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

val dotNetSdkGeneratedPropsFile = File(projectDir, "build/DotNetSdkPath.Generated.props")
val nuGetConfigFile = File(projectDir, "nuget.config")

fun File.writeTextIfChanged(content: String) {
    val bytes = content.toByteArray()

    if (!exists() || !readBytes().contentEquals(bytes)) {
        println("Writing $path")
        parentFile.mkdirs()
        writeBytes(bytes)
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    configure<com.jetbrains.rd.generator.gradle.RdGenExtension> {
        val modelDir = projectDir.resolve("protocol/src/main/kotlin/model")
        val pluginSourcePath = projectDir.resolve("src")

        verbose = true
        classpath({
            intellijPlatform.platformPath.resolve("lib/rd/rider-model.jar").toRealPath()
        })
        sources(modelDir)
        hashFolder = "$rootDir/build/rdgen/rider"
        packages = "model.meadowPlugin"

        val ktPluginOutput = pluginSourcePath.resolve("main/kotlin/com/jetbrains/rider/meadow/generated")
        val csPluginOutput = pluginSourcePath.resolve("dotnet/Meadow/Generated")
        generator {
            language = "kotlin"
            transform = "asis"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            directory = ktPluginOutput.canonicalPath
        }
        generator {
            language = "csharp"
            transform = "reversed"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            directory = csPluginOutput.canonicalPath
        }
    }

    val dotnetBuildConfiguration = properties("dotnetBuildConfiguration").get()

    val riderSdkPath by lazy {
        val path = intellijPlatform.platformPath.resolve("lib/DotNetSdkForRdPlugins")
        if (!path.isDirectory()) error("$path does not exist or not a directory")

        println("Rider SDK path: $path")
        return@lazy path
    }


    val generateDotNetSdkProperties by registering {
        doLast {
            dotNetSdkGeneratedPropsFile.writeTextIfChanged(
                """<Project>
  <PropertyGroup>
    <DotNetSdkPath>$riderSdkPath</DotNetSdkPath>
  </PropertyGroup>
</Project>
"""
            )
        }
    }

    val generateNuGetConfig by registering {
        doLast {
            nuGetConfigFile.writeTextIfChanged(
                """<?xml version="1.0" encoding="utf-8"?>
<configuration>
  <packageSources>
    <add key="rider-sdk" value="$riderSdkPath" />
  </packageSources>
</configuration>
"""
            )
        }
    }

    val publishDotnet by registering {
        dependsOn(rdgen, generateDotNetSdkProperties, generateNuGetConfig)
        doLast {
            exec {
                executable("dotnet")
                args("publish", "-c", dotnetBuildConfiguration, "/clp:ErrorsOnly", "MeadowPlugin.sln")
            }
        }
    }

    buildPlugin {
        dependsOn(publishDotnet)
    }

    register("prepare") {
        dependsOn(rdgen, generateDotNetSdkProperties, generateNuGetConfig)
    }

    prepareSandbox {
        dependsOn(publishDotnet)

        val outputFolder = file("$projectDir/src/dotnet/Meadow/bin/$dotnetBuildConfiguration/publish")
        val outputFolderPath = outputFolder.toPath()

        val files = outputFolder.walk().toList()

        for (fileName in files) {
            if (fileName.isDirectory) {
                continue
            }
//            if (fileName.name.startsWith("JetBrains.", ignoreCase = true) ||
//                fileName.name.startsWith("Debugger.", ignoreCase = true) ||
//                fileName.name.startsWith("Rider.", ignoreCase = true) ||
//                fileName.name.startsWith("Mono.", ignoreCase = true)) {
//                continue
//            }

            val relativeDirectory = outputFolderPath.relativize(fileName.parentFile.toPath())

            from(fileName) {
                into("${rootProject.name}/dotnet/${relativeDirectory}")
            }
        }
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }
    }

    signPlugin {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map {
            listOf(
                it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
        }
    }
}
