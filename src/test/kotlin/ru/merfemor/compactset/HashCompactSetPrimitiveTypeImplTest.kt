package ru.merfemor.compactset

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class HashCompactSetPrimitiveTypeImplTest {
    @Test
    fun `initial size is zero`() {
        val set = newCompactSet<Int>()
        Assertions.assertEquals(0, set.size)
    }

    @Test
    fun `contains on empty set is false`() {
        val set = newCompactSet<Int>()
        Assertions.assertFalse(set.contains(7))
    }

    @Test
    fun `contains zero on empty set is false`() {
        val set = newCompactSet<Int>()
        Assertions.assertFalse(set.contains(0))
    }

    @Test
    fun `add returns true when elements was not in set`() {
        val set = newCompactSet<Int>()
        Assertions.assertTrue(set.add(12))
    }

    @Test
    fun `size changes after add`() {
        val set = newCompactSet<Int>()
        set.add(12)
        Assertions.assertEquals(1, set.size)
    }

    @Test
    fun `duplicate element is not added`() {
        val set = newCompactSet<Int>()
        set.add(12)
        Assertions.assertFalse(set.add(12))
        Assertions.assertEquals(1, set.size)
    }

    @Test
    fun `add and contains work correctly with max value`() {
        val set = newCompactSet<Int>()
        Assertions.assertTrue(set.add(Int.MAX_VALUE))
        Assertions.assertTrue(set.contains(Int.MAX_VALUE))
        Assertions.assertEquals(1, set.size)
    }

    @Test
    fun `add and contains work correctly with min value`() {
        val set = newCompactSet<Int>()
        Assertions.assertTrue(set.add(Int.MIN_VALUE))
        Assertions.assertTrue(set.contains(Int.MIN_VALUE))
        Assertions.assertEquals(1, set.size)
    }

    @Test
    fun `add and contains work correctly with zero value`() {
        val set = newCompactSet<Int>()
        Assertions.assertTrue(set.add(0))
        Assertions.assertTrue(set.contains(0))
        Assertions.assertEquals(1, set.size)
    }

    @Test
    fun `zero is not added twice`() {
        val set = newCompactSet<Int>()
        Assertions.assertTrue(set.add(0))
        Assertions.assertFalse(set.add(0))
        Assertions.assertEquals(1, set.size)
    }

    @Test
    fun `add works if elements number exceed expectedSize`() {
        val set = newCompactSet<Int>(1)
        set.add(1)
        set.add(2)
        Assertions.assertEquals(2, set.size)
        Assertions.assertTrue(set.contains(1))
        Assertions.assertTrue(set.contains(2))
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