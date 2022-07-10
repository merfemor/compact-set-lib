package ru.merfemor.compactset

/**
 * Provides implementation class of [CompactSet] for given parameter type.
 * Should be used for primitive types.
 * This class is a singleton so as not to force the user to create an object and manage lifecycle on their own.
 */
internal class PrimitiveTypeCompactSetClassProvider {
    private val implClassesCache = mutableMapOf<String, Class<*>>()
    private val byteArrayClassLoader = ByteArrayClassLoader()

    /**
     * Returns implementation class of [CompactSet] for given type parameter [T].
     * Classes bytecode is generated in runtime on first method call. Next call with same [typeParameter]
     * will return previously generated class.
     */
    fun <T> getImplClass(typeParameter: Class<T>): Class<CompactSet<T>> {
        val implClass = implClassesCache.getOrPut(typeParameter.name) { generateImplClass(typeParameter) }
        @Suppress("UNCHECKED_CAST")
        return implClass as Class<CompactSet<T>>
    }

    private fun <T> generateImplClass(typeParameter: Class<T>): Class<CompactSet<T>> {
        val packageName = HashCompactSetGeneralImpl::class.java.packageName
        val parameterTypeName = typeParameter.canonicalName.replace('.', '_')
        val simpleName = "HashCompactSetImpl_$parameterTypeName"
        val canonicalName = "$packageName.$simpleName"
        val bytecodeGenerator = PrimitiveTypeCompactSetClassBytecodeGenerator(typeParameter, canonicalName)
        val bytecode = bytecodeGenerator.generateImplClassBytecode()
        return byteArrayClassLoader.defineClass(canonicalName, bytecode)
    }
}