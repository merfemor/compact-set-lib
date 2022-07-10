package ru.merfemor.compactset

/**
 * Class used for parametrized tests for different [CompactSet] parameter types.
 * Use [create] to create new instance.
 */
open class CompactSetTestEmptyArgs<T> protected constructor(
    private val typeParameter: Class<T>,
    private val newCompactSetRef: (Int) -> CompactSet<T>
) {
    fun newCompactSet(expectedSize: Int = 16) = newCompactSetRef(expectedSize)

    override fun toString(): String {
        return "CompactSetTestEmptyArgs($typeParameter)"
    }

    companion object {
        internal inline fun <reified T> create(): CompactSetTestEmptyArgs<T> {
            return CompactSetTestEmptyArgs(T::class.java, ::newCompactSet)
        }
    }
}

/**
 * Like [CompactSetTestEmptyArgs] but also holds one argument [value] of type [T].
 * Use [create] to create new instance.
 */
class CompactSetTestOneValueArgs<T> private constructor(
    typeParameter: Class<T>,
    newCompactSetRef: (Int) -> CompactSet<T>,
    val value: T
) : CompactSetTestEmptyArgs<T>(typeParameter, newCompactSetRef) {

    override fun toString(): String {
        return "CompactSetTestOneValueArgs($value)"
    }

    companion object {
        internal inline fun <reified T> create(value: T): CompactSetTestOneValueArgs<T> {
            return CompactSetTestOneValueArgs(T::class.java, ::newCompactSet, value)
        }
    }
}

/**
 * Like [CompactSetTestEmptyArgs] but also holds two arguments of type [T].
 * Use [create] to create new instance.
 */
class CompactSetTestTwoValueArgs<T> private constructor(
    typeParameter: Class<T>,
    newCompactSetRef: (Int) -> CompactSet<T>,
    val firstValue: T,
    val secondValue: T
) : CompactSetTestEmptyArgs<T>(typeParameter, newCompactSetRef) {

    override fun toString(): String {
        return "CompactSetTestTwoValueArgs($firstValue, $secondValue)"
    }

    companion object {
        internal inline fun <reified T> create(firstValue: T, secondValue: T): CompactSetTestTwoValueArgs<T> {
            return CompactSetTestTwoValueArgs(T::class.java, ::newCompactSet, firstValue, secondValue)
        }
    }
}