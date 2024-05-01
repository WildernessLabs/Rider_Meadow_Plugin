@file:Suppress("EXPERIMENTAL_API_USAGE","EXPERIMENTAL_UNSIGNED_LITERALS","PackageDirectoryMismatch","UnusedImport","unused","LocalVariableName","CanBeVal","PropertyName","EnumEntryName","ClassName","ObjectPropertyName","UnnecessaryVariable","SpellCheckingInspection")
package com.jetbrains.rider.meadow.generated

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
    private val _resetDevice: RdCall<String, Unit>
) : RdExtBase() {
    //companion
    
    companion object : ISerializersOwner {
        
        override fun registerSerializersCore(serializers: ISerializers)  {
            val classLoader = javaClass.classLoader
            serializers.register(LazyCompanionMarshaller(RdId(-1686321114733927496), classLoader, "com.jetbrains.rider.meadow.generated.MeadowDeploymentArgs"))
            serializers.register(LazyCompanionMarshaller(RdId(2758887227611271224), classLoader, "com.jetbrains.rider.meadow.generated.MeadowDeploymentResult"))
        }
        
        
        
        
        private val __StringListSerializer = FrameworkMarshallers.String.list()
        
        const val serializationHash = 5053242627264958697L
        
    }
    override val serializersOwner: ISerializersOwner get() = MeadowPluginModel
    override val serializationHash: Long get() = MeadowPluginModel.serializationHash
    
    //fields
    val getSerialPorts: IRdCall<Unit, List<String>> get() = _getSerialPorts
    val resetDevice: IRdCall<String, Unit> get() = _resetDevice
    //methods
    //initializer
    init {
        _getSerialPorts.async = true
    }
    
    init {
        bindableChildren.add("getSerialPorts" to _getSerialPorts)
        bindableChildren.add("resetDevice" to _resetDevice)
    }
    
    //secondary constructor
    internal constructor(
    ) : this(
        RdCall<Unit, List<String>>(FrameworkMarshallers.Void, __StringListSerializer),
        RdCall<String, Unit>(FrameworkMarshallers.String, FrameworkMarshallers.Void)
    )
    
    //equals trait
    //hash code trait
    //pretty print
    override fun print(printer: PrettyPrinter)  {
        printer.println("MeadowPluginModel (")
        printer.indent {
            print("getSerialPorts = "); _getSerialPorts.print(printer); println()
            print("resetDevice = "); _resetDevice.print(printer); println()
        }
        printer.print(")")
    }
    //deepClone
    override fun deepClone(): MeadowPluginModel   {
        return MeadowPluginModel(
            _getSerialPorts.deepClonePolymorphic(),
            _resetDevice.deepClonePolymorphic()
        )
    }
    //contexts
    //threading
    override val extThreading: ExtThreadingKind get() = ExtThreadingKind.Default
}
val com.jetbrains.rd.ide.model.Solution.meadowPluginModel get() = getOrCreateExtension("meadowPluginModel", ::MeadowPluginModel)



/**
 * #### Generated from [MeadowPluginModel.kt:13]
 */
class MeadowDeploymentArgs (
    val port: String,
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
            val port = buffer.readString()
            val appPath = buffer.readString()
            val debug = buffer.readBool()
            return MeadowDeploymentArgs(port, appPath, debug, projectKind, projectFilePath)
        }
        
        override fun write(ctx: SerializationCtx, buffer: AbstractBuffer, value: MeadowDeploymentArgs)  {
            com.jetbrains.rider.model.RunnableProjectKind.write(ctx, buffer, value.projectKind)
            buffer.writeString(value.projectFilePath)
            buffer.writeString(value.port)
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
        
        if (port != other.port) return false
        if (appPath != other.appPath) return false
        if (debug != other.debug) return false
        if (projectKind != other.projectKind) return false
        if (projectFilePath != other.projectFilePath) return false
        
        return true
    }
    //hash code trait
    override fun hashCode(): Int  {
        var __r = 0
        __r = __r*31 + port.hashCode()
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
            print("port = "); port.print(printer); println()
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
 * #### Generated from [MeadowPluginModel.kt:19]
 */
class MeadowDeploymentResult (
    val debugPort: Int,
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
            val debugPort = buffer.readInt()
            return MeadowDeploymentResult(debugPort, status)
        }
        
        override fun write(ctx: SerializationCtx, buffer: AbstractBuffer, value: MeadowDeploymentResult)  {
            buffer.writeEnum(value.status)
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
        
        other as MeadowDeploymentResult
        
        if (debugPort != other.debugPort) return false
        if (status != other.status) return false
        
        return true
    }
    //hash code trait
    override fun hashCode(): Int  {
        var __r = 0
        __r = __r*31 + debugPort.hashCode()
        __r = __r*31 + status.hashCode()
        return __r
    }
    //pretty print
    override fun print(printer: PrettyPrinter)  {
        printer.println("MeadowDeploymentResult (")
        printer.indent {
            print("debugPort = "); debugPort.print(printer); println()
            print("status = "); status.print(printer); println()
        }
        printer.print(")")
    }
    
    override fun toString() = PrettyPrinter().singleLine().also { print(it) }.toString()
    //deepClone
    //contexts
    //threading
}
