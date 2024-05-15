@file:Suppress("EXPERIMENTAL_API_USAGE","EXPERIMENTAL_UNSIGNED_LITERALS","PackageDirectoryMismatch","UnusedImport","unused","LocalVariableName","CanBeVal","PropertyName","EnumEntryName","ClassName","ObjectPropertyName","UnnecessaryVariable","SpellCheckingInspection")
package com.jetbrains.rider.plugins.meadow.model

import com.jetbrains.rd.framework.*
import com.jetbrains.rd.framework.base.*
import com.jetbrains.rd.framework.impl.*

import com.jetbrains.rd.util.lifetime.*
import com.jetbrains.rd.util.reactive.*
import com.jetbrains.rd.util.string.*
import com.jetbrains.rd.util.*
import kotlin.time.Duration
import kotlin.reflect.KClass
import kotlin.jvm.JvmStatic



/**
 * #### Generated from [MeadowPluginModel.kt:11]
 */
class MeadowPluginModel private constructor(
    private val _getSerialPorts: RdCall<CliRunnerInfo, List<String>>,
    private val _startDebugServer: RdCall<DebugServerInfo, Unit>,
    private val _terminate: RdCall<CliRunnerInfoOnPort, Unit>
) : RdExtBase() {
    //companion
    
    companion object : ISerializersOwner {
        
        override fun registerSerializersCore(serializers: ISerializers)  {
            val classLoader = javaClass.classLoader
            serializers.register(LazyCompanionMarshaller(RdId(3030399540646525931), classLoader, "com.jetbrains.rider.plugins.meadow.model.CliRunnerInfo"))
            serializers.register(LazyCompanionMarshaller(RdId(-8282157804764963125), classLoader, "com.jetbrains.rider.plugins.meadow.model.CliRunnerInfoOnPort"))
            serializers.register(LazyCompanionMarshaller(RdId(-1686321114733927496), classLoader, "com.jetbrains.rider.plugins.meadow.model.MeadowDeploymentArgs"))
            serializers.register(LazyCompanionMarshaller(RdId(2758887227611271224), classLoader, "com.jetbrains.rider.plugins.meadow.model.MeadowDeploymentResult"))
            serializers.register(LazyCompanionMarshaller(RdId(7163900993924547761), classLoader, "com.jetbrains.rider.plugins.meadow.model.DebugServerInfo"))
            serializers.register(LazyCompanionMarshaller(RdId(-1647358313454331833), classLoader, "com.jetbrains.rider.plugins.meadow.model.CliRunnerInfoBase_Unknown"))
        }
        
        
        
        
        private val __StringListSerializer = FrameworkMarshallers.String.list()
        
        const val serializationHash = -1141783146362402166L
        
    }
    override val serializersOwner: ISerializersOwner get() = MeadowPluginModel
    override val serializationHash: Long get() = MeadowPluginModel.serializationHash
    
    //fields
    val getSerialPorts: IRdCall<CliRunnerInfo, List<String>> get() = _getSerialPorts
    val startDebugServer: IRdCall<DebugServerInfo, Unit> get() = _startDebugServer
    val terminate: IRdCall<CliRunnerInfoOnPort, Unit> get() = _terminate
    //methods
    //initializer
    init {
        _getSerialPorts.async = true
        _startDebugServer.async = true
        _terminate.async = true
    }
    
    init {
        bindableChildren.add("getSerialPorts" to _getSerialPorts)
        bindableChildren.add("startDebugServer" to _startDebugServer)
        bindableChildren.add("terminate" to _terminate)
    }
    
    //secondary constructor
    internal constructor(
    ) : this(
        RdCall<CliRunnerInfo, List<String>>(CliRunnerInfo, __StringListSerializer),
        RdCall<DebugServerInfo, Unit>(DebugServerInfo, FrameworkMarshallers.Void),
        RdCall<CliRunnerInfoOnPort, Unit>(CliRunnerInfoOnPort, FrameworkMarshallers.Void)
    )
    
    //equals trait
    //hash code trait
    //pretty print
    override fun print(printer: PrettyPrinter)  {
        printer.println("MeadowPluginModel (")
        printer.indent {
            print("getSerialPorts = "); _getSerialPorts.print(printer); println()
            print("startDebugServer = "); _startDebugServer.print(printer); println()
            print("terminate = "); _terminate.print(printer); println()
        }
        printer.print(")")
    }
    //deepClone
    override fun deepClone(): MeadowPluginModel   {
        return MeadowPluginModel(
            _getSerialPorts.deepClonePolymorphic(),
            _startDebugServer.deepClonePolymorphic(),
            _terminate.deepClonePolymorphic()
        )
    }
    //contexts
    //threading
    override val extThreading: ExtThreadingKind get() = ExtThreadingKind.Default
}
val com.jetbrains.rd.ide.model.Solution.meadowPluginModel get() = getOrCreateExtension("meadowPluginModel", ::MeadowPluginModel)



/**
 * #### Generated from [MeadowPluginModel.kt:17]
 */
class CliRunnerInfo (
    cliPath: String
) : CliRunnerInfoBase (
    cliPath
) {
    //companion
    
    companion object : IMarshaller<CliRunnerInfo> {
        override val _type: KClass<CliRunnerInfo> = CliRunnerInfo::class
        override val id: RdId get() = RdId(3030399540646525931)
        
        @Suppress("UNCHECKED_CAST")
        override fun read(ctx: SerializationCtx, buffer: AbstractBuffer): CliRunnerInfo  {
            val cliPath = buffer.readString()
            return CliRunnerInfo(cliPath)
        }
        
        override fun write(ctx: SerializationCtx, buffer: AbstractBuffer, value: CliRunnerInfo)  {
            buffer.writeString(value.cliPath)
        }
        
        
    }
    //fields
    //methods
    //initializer
    //secondary constructor
    //equals trait
    override fun equals(other: Any?): Boolean  {
        if (this === other) return true
        if (other == null || other::class != this::class) return false
        
        other as CliRunnerInfo
        
        if (cliPath != other.cliPath) return false
        
        return true
    }
    //hash code trait
    override fun hashCode(): Int  {
        var __r = 0
        __r = __r*31 + cliPath.hashCode()
        return __r
    }
    //pretty print
    override fun print(printer: PrettyPrinter)  {
        printer.println("CliRunnerInfo (")
        printer.indent {
            print("cliPath = "); cliPath.print(printer); println()
        }
        printer.print(")")
    }
    
    override fun toString() = PrettyPrinter().singleLine().also { print(it) }.toString()
    //deepClone
    //contexts
    //threading
}


/**
 * #### Generated from [MeadowPluginModel.kt:13]
 */
abstract class CliRunnerInfoBase (
    val cliPath: String
) : IPrintable {
    //companion
    
    companion object : IAbstractDeclaration<CliRunnerInfoBase> {
        override fun readUnknownInstance(ctx: SerializationCtx, buffer: AbstractBuffer, unknownId: RdId, size: Int): CliRunnerInfoBase  {
            val objectStartPosition = buffer.position
            val cliPath = buffer.readString()
            val unknownBytes = ByteArray(objectStartPosition + size - buffer.position)
            buffer.readByteArrayRaw(unknownBytes)
            return CliRunnerInfoBase_Unknown(cliPath, unknownId, unknownBytes)
        }
        
        
    }
    //fields
    //methods
    //initializer
    //secondary constructor
    //equals trait
    //hash code trait
    //pretty print
    //deepClone
    //contexts
    //threading
}


class CliRunnerInfoBase_Unknown (
    cliPath: String,
    override val unknownId: RdId,
    val unknownBytes: ByteArray
) : CliRunnerInfoBase (
    cliPath
), IUnknownInstance {
    //companion
    
    companion object : IMarshaller<CliRunnerInfoBase_Unknown> {
        override val _type: KClass<CliRunnerInfoBase_Unknown> = CliRunnerInfoBase_Unknown::class
        override val id: RdId get() = RdId(-1647358313454331833)
        
        @Suppress("UNCHECKED_CAST")
        override fun read(ctx: SerializationCtx, buffer: AbstractBuffer): CliRunnerInfoBase_Unknown  {
            throw NotImplementedError("Unknown instances should not be read via serializer")
        }
        
        override fun write(ctx: SerializationCtx, buffer: AbstractBuffer, value: CliRunnerInfoBase_Unknown)  {
            buffer.writeString(value.cliPath)
            buffer.writeByteArrayRaw(value.unknownBytes)
        }
        
        
    }
    //fields
    //methods
    //initializer
    //secondary constructor
    //equals trait
    override fun equals(other: Any?): Boolean  {
        if (this === other) return true
        if (other == null || other::class != this::class) return false
        
        other as CliRunnerInfoBase_Unknown
        
        if (cliPath != other.cliPath) return false
        
        return true
    }
    //hash code trait
    override fun hashCode(): Int  {
        var __r = 0
        __r = __r*31 + cliPath.hashCode()
        return __r
    }
    //pretty print
    override fun print(printer: PrettyPrinter)  {
        printer.println("CliRunnerInfoBase_Unknown (")
        printer.indent {
            print("cliPath = "); cliPath.print(printer); println()
        }
        printer.print(")")
    }
    
    override fun toString() = PrettyPrinter().singleLine().also { print(it) }.toString()
    //deepClone
    //contexts
    //threading
}


/**
 * #### Generated from [MeadowPluginModel.kt:19]
 */
class CliRunnerInfoOnPort (
    val serialPort: String,
    cliPath: String
) : CliRunnerInfoBase (
    cliPath
) {
    //companion
    
    companion object : IMarshaller<CliRunnerInfoOnPort> {
        override val _type: KClass<CliRunnerInfoOnPort> = CliRunnerInfoOnPort::class
        override val id: RdId get() = RdId(-8282157804764963125)
        
        @Suppress("UNCHECKED_CAST")
        override fun read(ctx: SerializationCtx, buffer: AbstractBuffer): CliRunnerInfoOnPort  {
            val cliPath = buffer.readString()
            val serialPort = buffer.readString()
            return CliRunnerInfoOnPort(serialPort, cliPath)
        }
        
        override fun write(ctx: SerializationCtx, buffer: AbstractBuffer, value: CliRunnerInfoOnPort)  {
            buffer.writeString(value.cliPath)
            buffer.writeString(value.serialPort)
        }
        
        
    }
    //fields
    //methods
    //initializer
    //secondary constructor
    //equals trait
    override fun equals(other: Any?): Boolean  {
        if (this === other) return true
        if (other == null || other::class != this::class) return false
        
        other as CliRunnerInfoOnPort
        
        if (serialPort != other.serialPort) return false
        if (cliPath != other.cliPath) return false
        
        return true
    }
    //hash code trait
    override fun hashCode(): Int  {
        var __r = 0
        __r = __r*31 + serialPort.hashCode()
        __r = __r*31 + cliPath.hashCode()
        return __r
    }
    //pretty print
    override fun print(printer: PrettyPrinter)  {
        printer.println("CliRunnerInfoOnPort (")
        printer.indent {
            print("serialPort = "); serialPort.print(printer); println()
            print("cliPath = "); cliPath.print(printer); println()
        }
        printer.print(")")
    }
    
    override fun toString() = PrettyPrinter().singleLine().also { print(it) }.toString()
    //deepClone
    //contexts
    //threading
}


/**
 * #### Generated from [MeadowPluginModel.kt:32]
 */
data class DebugServerInfo (
    val runnerInfo: CliRunnerInfoOnPort,
    val debugPort: Int
) : IPrintable {
    //companion
    
    companion object : IMarshaller<DebugServerInfo> {
        override val _type: KClass<DebugServerInfo> = DebugServerInfo::class
        override val id: RdId get() = RdId(7163900993924547761)
        
        @Suppress("UNCHECKED_CAST")
        override fun read(ctx: SerializationCtx, buffer: AbstractBuffer): DebugServerInfo  {
            val runnerInfo = CliRunnerInfoOnPort.read(ctx, buffer)
            val debugPort = buffer.readInt()
            return DebugServerInfo(runnerInfo, debugPort)
        }
        
        override fun write(ctx: SerializationCtx, buffer: AbstractBuffer, value: DebugServerInfo)  {
            CliRunnerInfoOnPort.write(ctx, buffer, value.runnerInfo)
            buffer.writeInt(value.debugPort)
        }
        
        
    }
    //fields
    //methods
    //initializer
    //secondary constructor
    //equals trait
    override fun equals(other: Any?): Boolean  {
        if (this === other) return true
        if (other == null || other::class != this::class) return false
        
        other as DebugServerInfo
        
        if (runnerInfo != other.runnerInfo) return false
        if (debugPort != other.debugPort) return false
        
        return true
    }
    //hash code trait
    override fun hashCode(): Int  {
        var __r = 0
        __r = __r*31 + runnerInfo.hashCode()
        __r = __r*31 + debugPort.hashCode()
        return __r
    }
    //pretty print
    override fun print(printer: PrettyPrinter)  {
        printer.println("DebugServerInfo (")
        printer.indent {
            print("runnerInfo = "); runnerInfo.print(printer); println()
            print("debugPort = "); debugPort.print(printer); println()
        }
        printer.print(")")
    }
    //deepClone
    //contexts
    //threading
}


/**
 * #### Generated from [MeadowPluginModel.kt:23]
 */
class MeadowDeploymentArgs (
    val runnerInfo: CliRunnerInfoOnPort,
    val appPath: String,
    val debug: Boolean,
    projectKind: com.jetbrains.rider.model.RunnableProjectKind,
    projectFilePath: String
) : com.jetbrains.rider.model.DeploymentArgsBase (
    projectKind,
    projectFilePath
) {
    //companion
    
    companion object : IMarshaller<MeadowDeploymentArgs> {
        override val _type: KClass<MeadowDeploymentArgs> = MeadowDeploymentArgs::class
        override val id: RdId get() = RdId(-1686321114733927496)
        
        @Suppress("UNCHECKED_CAST")
        override fun read(ctx: SerializationCtx, buffer: AbstractBuffer): MeadowDeploymentArgs  {
            val projectKind = com.jetbrains.rider.model.RunnableProjectKind.read(ctx, buffer)
            val projectFilePath = buffer.readString()
            val runnerInfo = CliRunnerInfoOnPort.read(ctx, buffer)
            val appPath = buffer.readString()
            val debug = buffer.readBool()
            return MeadowDeploymentArgs(runnerInfo, appPath, debug, projectKind, projectFilePath)
        }
        
        override fun write(ctx: SerializationCtx, buffer: AbstractBuffer, value: MeadowDeploymentArgs)  {
            com.jetbrains.rider.model.RunnableProjectKind.write(ctx, buffer, value.projectKind)
            buffer.writeString(value.projectFilePath)
            CliRunnerInfoOnPort.write(ctx, buffer, value.runnerInfo)
            buffer.writeString(value.appPath)
            buffer.writeBool(value.debug)
        }
        
        
    }
    //fields
    //methods
    //initializer
    //secondary constructor
    //equals trait
    override fun equals(other: Any?): Boolean  {
        if (this === other) return true
        if (other == null || other::class != this::class) return false
        
        other as MeadowDeploymentArgs
        
        if (runnerInfo != other.runnerInfo) return false
        if (appPath != other.appPath) return false
        if (debug != other.debug) return false
        if (projectKind != other.projectKind) return false
        if (projectFilePath != other.projectFilePath) return false
        
        return true
    }
    //hash code trait
    override fun hashCode(): Int  {
        var __r = 0
        __r = __r*31 + runnerInfo.hashCode()
        __r = __r*31 + appPath.hashCode()
        __r = __r*31 + debug.hashCode()
        __r = __r*31 + projectKind.hashCode()
        __r = __r*31 + projectFilePath.hashCode()
        return __r
    }
    //pretty print
    override fun print(printer: PrettyPrinter)  {
        printer.println("MeadowDeploymentArgs (")
        printer.indent {
            print("runnerInfo = "); runnerInfo.print(printer); println()
            print("appPath = "); appPath.print(printer); println()
            print("debug = "); debug.print(printer); println()
            print("projectKind = "); projectKind.print(printer); println()
            print("projectFilePath = "); projectFilePath.print(printer); println()
        }
        printer.print(")")
    }
    
    override fun toString() = PrettyPrinter().singleLine().also { print(it) }.toString()
    //deepClone
    //contexts
    //threading
}


/**
 * #### Generated from [MeadowPluginModel.kt:29]
 */
class MeadowDeploymentResult (
    status: com.jetbrains.rider.model.DeploymentResultStatus
) : com.jetbrains.rider.model.DeploymentResultBase (
    status
) {
    //companion
    
    companion object : IMarshaller<MeadowDeploymentResult> {
        override val _type: KClass<MeadowDeploymentResult> = MeadowDeploymentResult::class
        override val id: RdId get() = RdId(2758887227611271224)
        
        @Suppress("UNCHECKED_CAST")
        override fun read(ctx: SerializationCtx, buffer: AbstractBuffer): MeadowDeploymentResult  {
            val status = buffer.readEnum<com.jetbrains.rider.model.DeploymentResultStatus>()
            return MeadowDeploymentResult(status)
        }
        
        override fun write(ctx: SerializationCtx, buffer: AbstractBuffer, value: MeadowDeploymentResult)  {
            buffer.writeEnum(value.status)
        }
        
        
    }
    //fields
    //methods
    //initializer
    //secondary constructor
    //equals trait
    override fun equals(other: Any?): Boolean  {
        if (this === other) return true
        if (other == null || other::class != this::class) return false
        
        other as MeadowDeploymentResult
        
        if (status != other.status) return false
        
        return true
    }
    //hash code trait
    override fun hashCode(): Int  {
        var __r = 0
        __r = __r*31 + status.hashCode()
        return __r
    }
    //pretty print
    override fun print(printer: PrettyPrinter)  {
        printer.println("MeadowDeploymentResult (")
        printer.indent {
            print("status = "); status.print(printer); println()
        }
        printer.print(")")
    }
    
    override fun toString() = PrettyPrinter().singleLine().also { print(it) }.toString()
    //deepClone
    //contexts
    //threading
}
