package ru.merfemor.compactset

/**
 * Set with API similar to [MutableSet], which only supports adding elements.
 * Implementation attempts to be more memory efficient than the standard Java HashSet.
 * @param T the type of elements contained in the set.
 */
interface CompactSet<T> {
    /**
     * Returns the size of the collection.
     */
    val size: Int

    /**
     * Checks if the specified element is contained in this collection.
     */
    operator fun contains(value: T): Boolean

    /**
     * Adds the specified element to the set.
     *
     * @return `true` if the element has been added, `false` if the element is already contained in the set.
     */
    fun add(element: T): Boolean
}