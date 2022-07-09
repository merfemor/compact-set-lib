package ru.merfemor.compactset

import java.util.function.Consumer

/**
 * General implementation of [CompactSet] which can be used for any type [T].
 * Implementation uses static array of references for storing hash table elements.
 */
internal class HashCompactSetGeneralImpl<T>(expectedSize: Int) : AbstractHashCompactSet<T>(expectedSize) {
    private lateinit var array: Array<Any?>

    override val hashTableSize: Int
        get() = array.size

    @Suppress("UNCHECKED_CAST")
    override fun getElementAt(index: Int): T? = array[index] as T

    override fun setElementAt(index: Int, element: T) {
        array[index] = element
    }

    override fun cellIsFree(index: Int): Boolean = array[index] == null

    override fun resizeHashTable(newSize: Int, transferOldElement: Consumer<T>) {
        if (!::array.isInitialized) {
            array = arrayOfNulls(newSize)
            return
        }
        val oldArray = array
        array = arrayOfNulls(newSize)
        oldArray.filterNotNull().forEach {
            @Suppress("UNCHECKED_CAST")
            transferOldElement.accept(it as T)
        }
    }
}