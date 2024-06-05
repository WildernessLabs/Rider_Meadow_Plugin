import com.jetbrains.plugin.structure.base.utils.isFile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.changelog.exceptions.MissingVersionException
import org.jetbrains.intellij.platform.gradle.Constants
import org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.io.path.absolute
import kotlin.io.path.isDirectory

plugins {
    alias(libs.plugins.changelog)
    alias(libs.plugins.gradleIntelliJPlatform)
    alias(libs.plugins.gradleJvmWrapper)
    alias(libs.plugins.kotlinJvm)
    id("java")
}

allprojects {
    repositories {
        mavenCentral()
    }
}

repositories {
    intellijPlatform {
        defaultRepositories()
        jetbrainsRuntime()
    }
}

val pluginVersion: String by project
val riderSdkVersion: String by project
val untilBuildVersion: String by project
val buildConfiguration: String by project
val dotNetPluginId: String by project

val dotNetSrcDir = File(projectDir, "src/dotnet")
val dotNetProjectPath = File("$dotNetSrcDir/$dotNetPluginId/$dotNetPluginId.csproj")

version = pluginVersion

val riderSdkPath by lazy {
    val path = intellijPlatform.platformPath.resolve("lib/DotNetSdkForRdPlugins").absolute()
    if (!path.isDirectory()) error("$path does not exist or not a directory")

    println("Rider SDK path: $path")
    return@lazy path
}

dependencies {
    intellijPlatform {
        rider(riderSdkVersion)
        jetbrainsRuntime()
        instrumentationTools()
        bundledLibrary("lib/testFramework.jar")
    }
    testImplementation(libs.openTest4J)
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

sourceSets {
    main {
        kotlin.srcDir("src/rider/generated/kotlin")
        kotlin.srcDir("src/rider/main/kotlin")
        resources.srcDir("src/rider/main/resources")
    }
}

intellijPlatform {
    buildSearchableOptions = false
}

tasks {
    val generateDotNetSdkProperties by registering {
        val dotNetSdkGeneratedPropsFile = File(projectDir, "build/DotNetSdkPath.Generated.props")
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
        val nuGetConfigFile = File(dotNetSrcDir, "nuget.config")
        doLast {
            nuGetConfigFile.writeTextIfChanged(
                """
            <?xml version="1.0" encoding="utf-8"?>
            <!-- Auto-generated from 'generateNuGetConfig' task of old.build_gradle.kts -->
            <!-- Run `gradlew :prepare` to regenerate -->
            <configuration>
                <packageSources>
                    <add key="rider-sdk" value="$riderSdkPath" />
                </packageSources>
            </configuration>
            """.trimIndent()
            )
        }
    }

    val rdGen = ":protocol:rdgen"

    register("prepare") {
        dependsOn(rdGen, generateDotNetSdkProperties, generateNuGetConfig)
    }

    val publishDotnet by registering {
        dependsOn(rdGen, generateDotNetSdkProperties, generateNuGetConfig)
        doLast {
            exec {
                executable("dotnet-sdk.cmd")
                args("publish", "-c", buildConfiguration, dotNetProjectPath)
            }
        }
    }

    withType<KotlinCompile> {
        dependsOn(rdGen)
    }

    buildPlugin {
        dependsOn(publishDotnet)
    }

    patchPluginXml {
        untilBuild.set(untilBuildVersion)
        val latestChangelog = try {
            changelog.getUnreleased()
        } catch (_: MissingVersionException) {
            changelog.getLatest()
        }
        changeNotes.set(provider {
            changelog.renderItem(
                latestChangelog
                    .withHeader(false)
                    .withEmptySections(false),
                org.jetbrains.changelog.Changelog.OutputType.HTML
            )
        })
    }

    withType<PrepareSandboxTask> {
        dependsOn(publishDotnet)

        val outputFolder = file("$dotNetSrcDir/$dotNetPluginId/bin/$buildConfiguration/publish")

        from(outputFolder) {
            into("${rootProject.name}/net-v2")
            exclude(
                "**/*/System.Drawing.Common.dll",
                "Microsoft.Extensions.DependencyInjection.Abstractions.dll",
                "Microsoft.Extensions.DependencyInjection.dll",
                "Microsoft.Extensions.Logging.Abstractions.dll",
                "Microsoft.Extensions.Logging.dll",
                "Microsoft.Extensions.Options.dll",
                "Microsoft.Extensions.Primitives.dll",
                "Microsoft.IdentityModel.JsonWebTokens.dll",
                "Microsoft.IdentityModel.Logging.dll",
                "Microsoft.IdentityModel.Tokens.dll",
                "Microsoft.Win32.SystemEvents.dll",
                "Mono.Cecil.dll",
                "Mono.Cecil.Mdb.dll",
                "Mono.Cecil.Pdb.dll",
                "Mono.Cecil.Rocks.dll",
                "Newtonsoft.Json.dll",
                "System.CodeDom.dll",
                "System.Configuration.ConfigurationManager.dll",
                "System.Diagnostics.EventLog.dll",
                "System.Drawing.Common.dll",
                "System.IO.Ports.dll",
                "System.Security.Cryptography.ProtectedData.dll",
                "System.Security.Permissions.dll",
                "System.Windows.Extensions.dll",
                "YamlDotNet.dll"
            )
        }

        val templateBaseDir = projectDir.parentFile.resolve("Meadow.Sdk/Meadow_DotNet_SDK/Project_Templates/templates")
        for (template in templateBaseDir.listFiles() ?: emptyArray()) {
            from(template) {
                into("${rootProject.name}/projectTemplates/${template.name}")
            }
        }

    }

    runIde {
        jvmArgs("-Xmx1500m")
    }

    test {
        useTestNG()
        testLogging {
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
        environment["LOCAL_ENV_RUN"] = "true"
    }
}

val riderModel: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(riderModel.name, provider {
        intellijPlatform.platformPath.resolve("lib/rd/rider-model.jar").also {
            check(it.isFile) {
                "rider-model.jar is not found at $riderModel"
            }
        }
    }) {
        builtBy(Constants.Tasks.INITIALIZE_INTELLIJ_PLATFORM_PLUGIN)
    }
}

fun File.writeTextIfChanged(content: String) {
    val bytes = content.toByteArray()

    if (!exists() || !readBytes().contentEquals(bytes)) {
        println("Writing $path")
        parentFile.mkdirs()
        writeBytes(bytes)
    }
}
