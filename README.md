<img src="Design/wildernesslabs-meadow-vscode-extension.jpg" style="margin-bottom:10px" />

# Rider_Meadow_Plugin

This is the plugin for Rider that enables Meadow apps to be built, debugged and deployed to a Meadow device.

# Build Status
[![Build](https://github.com/WildernessLabs/Rider_Meadow_Plugin/actions/workflows/main.yml/badge.svg)](https://github.com/WildernessLabs/Rider_Meadow_Plugin/actions)


# Download Plugin
[![Rider Plugin for Meadow](https://img.shields.io/jetbrains/plugin/v/RiderMeadowPlugin.svg?label=RiderMeadowPlugin&colorB=0A7BBB&style=for-the-badge&logo=rider)](https://plugins.jetbrains.com/plugin/RiderMeadowPlugin)

# Download Rider
[<img src="https://www.jetbrains.com/guide/assets/logo-135a4cec.png" alt="Rider IDE" width="100" height="100">](https://www.jetbrains.com/rider/download/)


## Release Notes

### 1.0.0

Initial Release of the Rider plugin

## Getting Started

If you just want to start developing the plugin and don't want to build it (yet), then execute this shell command:

```console
$ ./gradlew prepare
```

This will download the initial set of dependencies necessary for the plugin development and set up Rider SDK for .NET part of the project. After that, open either the frontend part of the plugin (the directory containing `build.gradle.kts`) using IntelliJ IDEA, or the `Rider_Meadow_Plugin.sln` using Rider.

[gradle-jvm-wrapper]: https://github.com/mfilippov/gradle-jvm-wrapper

## Contributer Guide

### Prerequisites

downloaded automatically during the build
- .NET SDK 8.0 or later.

### Build

#### Clone:

```console
git clone git@github.com/WildernessLabs/Meadow.CLI.git
```
in sibling folder to this repository


To build the plugin, execute this shell command:

```console
$ ./gradlew buildPlugin
```

This action will use [Gradle JVM Wrapper][gradle-jvm-wrapper] to automatically
download the recommended JDK version that's used for builds, and will download a
required Gradle version. If this isn't necessary, you could use your own
versions of Gradle and JRE by running the build task with `gradle buildPlugin`.

After that, the plugin ZIP distribution will be created in the
`build/distributions` directory.

### Run IDE

The following command will build the plugin and run it using a sandboxed
instance of Rider (set the required version via `build.gradle`).

```console
$ ./gradlew runIde
```

### Test

Execute the following shell command:

```console
$ ./gradlew :check
```

Development
-----------

## IntelliJ IDEA Setup

After running `./gradlew` at least once, set up your project SDK to the following folder:

- `%LOCALAPPDATA%\gradle-jvm` (Windows),
- `${HOME}/.local/share/gradle-jvm` (Unix-based OS).

This JDK is guaranteed to contain all the components necessary to build the plugin.

## Architecture

This plugin consists of two parts: the backend one (written in C#) and the frontend one (written in Kotlin). Each part requires a corresponding IDE. To develop the backend, it's recommended to open `Rider_Meadow_Plugin.sln` with JetBrains Rider. To develop a frontend, it's recommended to use IntelliJ IDEA (Community edition should be enough).