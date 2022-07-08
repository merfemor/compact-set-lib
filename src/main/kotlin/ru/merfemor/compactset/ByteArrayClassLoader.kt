package ru.merfemor.compactset

/**
 * Utility to create class from bytecode array.
 * Such method exists in [ClassLoader], but has protected access.
 */
internal class ByteArrayClassLoader : ClassLoader() {
    fun <T> defineClass(className: String, array: ByteArray): Class<T> {
        @Suppress("UNCHECKED_CAST")
        return defineClass(className, array, 0, array.size) as Class<T>
    }
}