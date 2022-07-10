package ru.merfemor.compactset

import org.objectweb.asm.Type
import kotlin.reflect.KClass

internal fun String.toInternalName(): String = replace('.', '/')

internal val <T> Class<T>.internalName: String
    get() = Type.getInternalName(this)

internal val <T : Any> KClass<T>.internalName: String
    get() = java.internalName

internal val <T> Class<T>.descriptor: String
    get() = Type.getDescriptor(this)

internal val <T : Any> KClass<T>.descriptor: String
    get() = java.descriptor