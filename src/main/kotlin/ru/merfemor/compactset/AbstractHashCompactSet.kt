package ru.merfemor.compactset

import java.util.function.Consumer
import kotlin.math.min

/**
 * Abstract implementation of [CompactSet] based on hash table with open addressing.
 * This class abstracts from how hash table elements are stored and leaves that decision to descendants.
 */
internal abstract class AbstractHashCompactSet<T>(expectedSize: Int) : CompactSet<T> {
    // We don't store null in hash table and handle it separately
    // because null value might be clashed with "free hash table cell" meaning.
    private var nullAdded: Boolean = false
    override var size = 0
        protected set

    init {
        if (expectedSize < 0) {
            throw IllegalArgumentException("expectedSize must be >= 0 but actual $expectedSize")
        }
        // We want to prevent resize when "expectedSize" elements will be added,
        // so we set initial capacity to be with extra space to not exceed load factor.
        val sizeWithExtraSpace = (expectedSize / LOAD_FACTOR).toLong() + 1
        val initialCapacity = min(MAX_CAPACITY.toLong(), sizeWithExtraSpace).toInt()
        resizeHashTable(initialCapacity, ::transferElement)
    }

    /**
     * @return current size of hash table
     */
    protected abstract val hashTableSize: Int

    /**
     * @return true if cell at [index] in hash table is free, false otherwise
     */
    protected abstract fun cellIsFree(index: Int): Boolean

    /**
     * @return get element of hash table at [index] or null if cell is free
     */
    protected abstract fun getElementAt(index: Int): T?

    /**
     * Set element of hash table at [index] to value [element].
     * Argument [element] is always not null here even if [T] is nullable.
     */
    protected abstract fun setElementAt(index: Int, element: T)

    /**
     * Implementations must resize hash table to fit specified [newSize] and call [transferOldElement] on each
     * of old elements stored hash table to recalculate indexes.
     */
    protected abstract fun resizeHashTable(newSize: Int, transferOldElement: Consumer<T>)

    override fun add(element: T): Boolean {
        if (element == null) {
            return if (nullAdded) {
                false
            } else {
                nullAdded = true
                size++
                true
            }
        }
        if (hashTableSize == 0) {
            resize()
        }
        val i = findIndexForKey(element)
        if (!cellIsFree(i)) {
            return false
        }
        setElementAt(i, element)
        size++
        if (needToResize()) {
            resize()
        }
        return true
    }

    override fun contains(value: T): Boolean {
        if (value == null) {
            return nullAdded
        }
        if (hashTableSize == 0) {
            return false
        }
        val i = findIndexForKey(value)
        return !cellIsFree(i)
    }

    /**
     * If the element is already in the set, returns the index of that element,
     * otherwise returns the index where to store the new element.
     */
    private fun findIndexForKey(element: T): Int {
        if (hashTableSize == 0) {
            throw IllegalStateException("array should not be empty here")
        }
        if (element == null) {
            throw IllegalStateException("only non-null element allowed here")
        }
        var hash = element.hashCode()
        if (hash < 0) {
            hash = -hash
        }
        var i = hash % hashTableSize

        while (!cellIsFree(i) && getElementAt(i) != element) {
            i = (i + INDEX_INCREASE_STEP) % hashTableSize
        }
        return i
    }

    private fun needToResize(): Boolean {
        return size.toFloat() / hashTableSize >= LOAD_FACTOR
    }

    private fun resize() {
        val newSize = calculateNewSize()
        resizeHashTable(newSize, ::transferElement)
    }

    private fun transferElement(element: T) {
        val newI = findIndexForKey(element)
        setElementAt(newI, element)
    }

    private fun calculateNewSize(): Int {
        val curSize = hashTableSize
        if (curSize >= MAX_CAPACITY) {
            throw IllegalStateException("Collection size = $curSize which is above max capacity $MAX_CAPACITY")
        }
        if (curSize == 0) {
            return SIZE_AFTER_0
        }
        return if (MAX_CAPACITY / 2 <= curSize) {
            MAX_CAPACITY
        } else {
            curSize * 2
        }
    }

    // visible for testing
    internal companion object {
        // The percentage of the table being filled after which resize is needed
        const val LOAD_FACTOR = 0.7
        // Max possible size of hash table
        const val MAX_CAPACITY = Int.MAX_VALUE
        // New size of hash table if previous size was 0
        private const val SIZE_AFTER_0 = 1
        // Used in case of collisions to choose the index in which to store the value
        private const val INDEX_INCREASE_STEP = 1
    }
}