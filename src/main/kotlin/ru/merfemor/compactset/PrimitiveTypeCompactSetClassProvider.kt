package ru.merfemor.compactset

/**
 * Provides implementation class of [CompactSet] for given parameter type.
 * Should be used for primitive types.
 * This class is a singleton so as not to force the user to create an object and manage lifecycle on their own.
 */
internal object PrimitiveTypeCompactSetClassProvider {
    private val implClassesCache = mutableMapOf<String, Class<*>>()
    private val byteArrayClassLoader = ByteArrayClassLoader()
    private val primitiveTypeCompactSetClassGenerator = PrimitiveTypeCompactSetClassGenerator()

    /**
     * Returns implementation class of [CompactSet] for given type parameter [T].
     * Classes bytecode is generated in runtime on first method call. Next call with same [typeParameter]
     * will return previously generated class.
     */
    fun <T> getImplClassFactory(typeParameter: Class<T>): Class<CompactSet<T>> {
        val implClassFactory = implClassesCache.getOrPut(typeParameter.name) { generateImplClass(typeParameter) }
        @Suppress("UNCHECKED_CAST")
        return implClassFactory as Class<CompactSet<T>>
    }

    private fun <T> generateImplClass(typeParameter: Class<T>): Class<CompactSet<T>> {
        val bytecode = primitiveTypeCompactSetClassGenerator.generateImplClassBytecodeForType(typeParameter)
        val implClassName = getImplClassName(typeParameter)
        return byteArrayClassLoader.defineClass(implClassName, bytecode)
    }

    private fun <T> getImplClassName(typeParameter: Class<T>): String {
        val generalImplName = CompactSetGeneralImpl::class.qualifiedName!!
        val dotIndex = generalImplName.indexOfLast { it == '.' }
        val packageName = generalImplName.subSequence(0, dotIndex)
        val parameterTypeName = typeParameter.name.replace('.', '_')
        return "$packageName.CompactSetImpl_$parameterTypeName"
    }
}