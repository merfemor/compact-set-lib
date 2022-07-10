package ru.merfemor.compactset

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class HashCompactSetPrimitiveTypeImplTest {
    @Test
    fun `initial size is zero`() {
        val set = newCompactSet<Int>()
        Assertions.assertEquals(0, set.size)
    }

    @ParameterizedTest
    @ValueSource(ints = [12, 0, Int.MAX_VALUE, Int.MIN_VALUE])
    fun `contains on empty set is false`(value: Int) {
        val set = newCompactSet<Int>()
        Assertions.assertFalse(set.contains(value))
    }

    @ParameterizedTest
    @ValueSource(ints = [12, 0, Int.MAX_VALUE, Int.MIN_VALUE])
    fun `add returns true when element was not in set`(value: Int) {
        val set = newCompactSet<Int>()
        Assertions.assertTrue(set.add(value))
    }

    @ParameterizedTest
    @ValueSource(ints = [12, 0, Int.MAX_VALUE, Int.MIN_VALUE])
    fun `size changes after add`(value: Int) {
        val set = newCompactSet<Int>()
        set.add(value)
        Assertions.assertEquals(1, set.size)
    }

    @ParameterizedTest
    @ValueSource(ints = [12, 0, Int.MAX_VALUE, Int.MIN_VALUE])
    fun `contains returns true after add`(value: Int) {
        val set = newCompactSet<Int>()
        set.add(value)
        Assertions.assertTrue(set.contains(value))
    }

    @ParameterizedTest
    @ValueSource(ints = [12, 0])
    fun `duplicate element is not added`(value: Int) {
        val set = newCompactSet<Int>()
        set.add(value)
        Assertions.assertFalse(set.add(value))
        Assertions.assertEquals(1, set.size)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 12])
    fun `add works if elements number exceed expectedSize`(secondElement: Int) {
        val set = newCompactSet<Int>(1)
        set.add(1)
        set.add(secondElement)
        Assertions.assertEquals(2, set.size)
        Assertions.assertTrue(set.contains(1))
        Assertions.assertTrue(set.contains(secondElement))
    }

    @Test
    fun `correctly works when expectedSize is 0`() {
        val set = newCompactSet<Int>(0)
        set.add(1)
        Assertions.assertEquals(1, set.size)
        Assertions.assertTrue(set.contains(1))
    }

    // TODO: collision tests
    // TODO: null tests
    // TODO: parametrize and check for Double + Long
}