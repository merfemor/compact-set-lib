package ru.merfemor.compactset

import kotlin.math.min

/**
 * General implementation of [CompactSet] which can be used for any type.
 * Implementation is based on hash table with open addressing stored in static array.
 */
internal class CompactSetGeneralImpl<T>(expectedSize: Int) : CompactSet<T> {
    private var array: Array<Any?>
    // we can't check presence by comparing to null, because "null" also can be added into set
    private var isPresent: BooleanArray
    override var size = 0
        private set

    init {
        if (expectedSize < 0) {
            throw IllegalArgumentException("expectedSize must be >= 0 but actual $expectedSize")
        }
        // We want to prevent resize, so we set initial capacity to be with extra space to not exceed load factor
        // when "expectedSize" elements will be added.
        val sizeWithExtraSpace = (expectedSize / LOAD_FACTOR).toLong() + 1
        val initialCapacity = min(MAX_CAPACITY.toLong(), sizeWithExtraSpace).toInt()
        array = Array(initialCapacity) { null }
        isPresent = BooleanArray(initialCapacity)
    }

    override fun add(element: T): Boolean {
        if (array.isEmpty()) {
            resize()
        }
        val i = findIndexForKey(element)
        if (isPresent[i]) {
            return false
        }
        array[i] = element
        isPresent[i] = true
        size++
        if (needToResize()) {
            resize()
        }
        return true
    }

    override fun contains(value: T): Boolean {
        if (array.isEmpty()) {
            return false
        }
        val i = findIndexForKey(value)
        return isPresent[i]
    }

    /**
     * If the element is already in the set, returns the index of that element,
     * otherwise returns the index where to store the new element.
     */
    private fun findIndexForKey(element: T): Int {
        if (array.isEmpty()) {
            throw IllegalStateException("array should not be empty here")
        }
        var hash = element?.hashCode() ?: NULL_HASH_VALUE
        if (hash < 0) {
            hash = -hash
        }
        var i = hash % array.size

        while (isPresent[i] && array[i] != element) {
            i = (i + INDEX_INCREASE_STEP) % array.size
        }
        return i
    }

    private fun needToResize(): Boolean {
        return size.toFloat() / array.size >= LOAD_FACTOR
    }

    private fun resize() {
        val newSize = calculateNewSize()
        val oldArray = array
        val oldIsPresent = isPresent
        array = Array(newSize) { null }
        isPresent = BooleanArray(newSize)

        for (i in oldArray.indices) {
            if (oldIsPresent[i]) {
                @Suppress("UNCHECKED_CAST")
                val element = oldArray[i] as T
                val newI = findIndexForKey(element)
                array[newI] = element
                isPresent[newI] = true
            }
        }
    }

    private fun calculateNewSize(): Int {
        val curSize = array.size
        if (curSize >= MAX_CAPACITY) {
            throw IllegalStateException("Collection size = $curSize which is above max capacity $MAX_CAPACITY")
        }
        if (curSize == 0) {
            return INITIAL_SIZE
        }
        return if (MAX_CAPACITY / 2 <= curSize) {
            MAX_CAPACITY
        } else {
            curSize * 2
        }
    }

    // visible for testing
    internal companion object {
        private const val INDEX_INCREASE_STEP = 1
        const val LOAD_FACTOR = 0.7
        const val MAX_CAPACITY = Int.MAX_VALUE
        private const val INITIAL_SIZE = 1
        const val NULL_HASH_VALUE = 0
    }
}