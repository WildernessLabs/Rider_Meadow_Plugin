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
    private val _getSerialPorts: RdCall<Unit, List<String>>,
    private val _startDebugServer: RdCall<DebugServerInfo, Unit>,
    private val _terminate: RdCall<DeviceModel, Unit>
) : RdExtBase() {
    //companion
    
    companion object : ISerializersOwner {
        
        override fun registerSerializersCore(serializers: ISerializers)  {
            val classLoader = javaClass.classLoader
            serializers.register(LazyCompanionMarshaller(RdId(541269807180439648), classLoader, "com.jetbrains.rider.plugins.meadow.model.DeviceModel"))
            serializers.register(LazyCompanionMarshaller(RdId(-1686321114733927496), classLoader, "com.jetbrains.rider.plugins.meadow.model.MeadowDeploymentArgs"))
            serializers.register(LazyCompanionMarshaller(RdId(2758887227611271224), classLoader, "com.jetbrains.rider.plugins.meadow.model.MeadowDeploymentResult"))
            serializers.register(LazyCompanionMarshaller(RdId(7163900993924547761), classLoader, "com.jetbrains.rider.plugins.meadow.model.DebugServerInfo"))
        }
        
        
        
        
        private val __StringListSerializer = FrameworkMarshallers.String.list()
        
        const val serializationHash = -4979951659644148378L
        
    }
    override val serializersOwner: ISerializersOwner get() = MeadowPluginModel
    override val serializationHash: Long get() = MeadowPluginModel.serializationHash
    
    //fields
    val getSerialPorts: IRdCall<Unit, List<String>> get() = _getSerialPorts
    val startDebugServer: IRdCall<DebugServerInfo, Unit> get() = _startDebugServer
    val terminate: IRdCall<DeviceModel, Unit> get() = _terminate
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
        RdCall<Unit, List<String>>(FrameworkMarshallers.Void, __StringListSerializer),
        RdCall<DebugServerInfo, Unit>(DebugServerInfo, FrameworkMarshallers.Void),
        RdCall<DeviceModel, Unit>(DeviceModel, FrameworkMarshallers.Void)
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
 * #### Generated from [MeadowPluginModel.kt:26]
 */
data class DebugServerInfo (
    val device: DeviceModel,
    val debugPort: Int
) : IPrintable {
    //companion
    
    companion object : IMarshaller<DebugServerInfo> {
        override val _type: KClass<DebugServerInfo> = DebugServerInfo::class
        override val id: RdId get() = RdId(7163900993924547761)
        
        @Suppress("UNCHECKED_CAST")
        override fun read(ctx: SerializationCtx, buffer: AbstractBuffer): DebugServerInfo  {
            val device = DeviceModel.read(ctx, buffer)
            val debugPort = buffer.readInt()
            return DebugServerInfo(device, debugPort)
        }
        
        override fun write(ctx: SerializationCtx, buffer: AbstractBuffer, value: DebugServerInfo)  {
            DeviceModel.write(ctx, buffer, value.device)
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
        
        if (device != other.device) return false
        if (debugPort != other.debugPort) return false
        
        return true
    }
    //hash code trait
    override fun hashCode(): Int  {
        var __r = 0
        __r = __r*31 + device.hashCode()
        __r = __r*31 + debugPort.hashCode()
        return __r
    }
    //pretty print
    override fun print(printer: PrettyPrinter)  {
        printer.println("DebugServerInfo (")
        printer.indent {
            print("device = "); device.print(printer); println()
            print("debugPort = "); debugPort.print(printer); println()
        }
        printer.print(")")
    }
    //deepClone
    //contexts
    //threading
}


/**
 * #### Generated from [MeadowPluginModel.kt:13]
 */
data class DeviceModel (
    val serialPort: String
) : IPrintable {
    //companion
    
    companion object : IMarshaller<DeviceModel> {
        override val _type: KClass<DeviceModel> = DeviceModel::class
        override val id: RdId get() = RdId(541269807180439648)
        
        @Suppress("UNCHECKED_CAST")
        override fun read(ctx: SerializationCtx, buffer: AbstractBuffer): DeviceModel  {
            val serialPort = buffer.readString()
            return DeviceModel(serialPort)
        }
        
        override fun write(ctx: SerializationCtx, buffer: AbstractBuffer, value: DeviceModel)  {
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
        
        other as DeviceModel
        
        if (serialPort != other.serialPort) return false
        
        return true
    }
    //hash code trait
    override fun hashCode(): Int  {
        var __r = 0
        __r = __r*31 + serialPort.hashCode()
        return __r
    }
    //pretty print
    override fun print(printer: PrettyPrinter)  {
        printer.println("DeviceModel (")
        printer.indent {
            print("serialPort = "); serialPort.print(printer); println()
        }
        printer.print(")")
    }
    //deepClone
    //contexts
    //threading
}


/**
 * #### Generated from [MeadowPluginModel.kt:17]
 */
class MeadowDeploymentArgs (
    val device: DeviceModel,
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
            val device = DeviceModel.read(ctx, buffer)
            val appPath = buffer.readString()
            val debug = buffer.readBool()
            return MeadowDeploymentArgs(device, appPath, debug, projectKind, projectFilePath)
        }
        
        override fun write(ctx: SerializationCtx, buffer: AbstractBuffer, value: MeadowDeploymentArgs)  {
            com.jetbrains.rider.model.RunnableProjectKind.write(ctx, buffer, value.projectKind)
            buffer.writeString(value.projectFilePath)
            DeviceModel.write(ctx, buffer, value.device)
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
        
        if (device != other.device) return false
        if (appPath != other.appPath) return false
        if (debug != other.debug) return false
        if (projectKind != other.projectKind) return false
        if (projectFilePath != other.projectFilePath) return false
        
        return true
    }
    //hash code trait
    override fun hashCode(): Int  {
        var __r = 0
        __r = __r*31 + device.hashCode()
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
            print("device = "); device.print(printer); println()
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
 * #### Generated from [MeadowPluginModel.kt:23]
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
