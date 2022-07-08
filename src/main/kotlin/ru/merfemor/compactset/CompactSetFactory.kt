package ru.merfemor.compactset


/**
 * Creates new compact set. Implementation is chosen based on type parameter [T].
 * @param expectedSize the expected number of elements to be placed in set. Must be >= 0.
 */
inline fun <reified T> newCompactSet(expectedSize: Int = 16): CompactSet<T> {
    if (expectedSize < 0) {
        throw IllegalArgumentException("expectedSize must be >= 0 but actual $expectedSize")
    }
    return when (T::class) {
        Int::class, Double::class, Long::class -> newPrimitiveTypeCompactSet(expectedSize, T::class.java)
        else -> newGeneralTypeCompactSet(expectedSize)
    }
}

fun <T> newGeneralTypeCompactSet(expectedSize: Int): CompactSet<T> {
    return CompactSetGeneralImpl(expectedSize)
}

fun <T> newPrimitiveTypeCompactSet(expectedSize: Int, typeParameter: Class<T>): CompactSet<T> {
    val compactSetImplClass = PrimitiveTypeCompactSetClassProvider.getImplClassFactory(typeParameter)
    val constructor = compactSetImplClass.getDeclaredConstructor(Integer.TYPE)
    return constructor.newInstance(expectedSize)
}
