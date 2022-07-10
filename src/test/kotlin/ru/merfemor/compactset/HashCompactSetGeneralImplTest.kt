package ru.merfemor.compactset

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource

class HashCompactSetGeneralImplTest {

    @Test
    fun `initial size is zero`() {
        val set = newCompactSet<String>()
        Assertions.assertEquals(0, set.size)
    }

    @ParameterizedTest
    @ValueSource(strings = ["some string"])
    @NullAndEmptySource
    fun `contains on empty set is false`(value: String?) {
        val set = newCompactSet<String?>()
        println("value is $value")
        Assertions.assertFalse(set.contains(value))
    }

    @ParameterizedTest
    @ValueSource(strings = ["some string"])
    @NullAndEmptySource
    fun `add returns true when element was not in set`(value: String?) {
        val set = newCompactSet<String?>()
        Assertions.assertTrue(set.add(value))
    }

    @ParameterizedTest
    @ValueSource(strings = ["some string"])
    @NullAndEmptySource
    fun `size changes after add`(value: String?) {
        val set = newCompactSet<String?>()
        set.add(value)
        Assertions.assertEquals(1, set.size)
    }

    @ParameterizedTest
    @ValueSource(strings = ["some string"])
    @NullAndEmptySource
    fun `contains returns true after add`(value: String?) {
        val set = newCompactSet<String?>()
        set.add(value)
        Assertions.assertTrue(set.contains(value))
    }

    @ParameterizedTest
    @ValueSource(strings = ["some string"])
    @NullAndEmptySource
    fun `duplicate element is not added`(value: String?) {
        val set = newCompactSet<String?>()
        set.add(value)
        Assertions.assertFalse(set.add(value))
        Assertions.assertEquals(1, set.size)
    }

    @ParameterizedTest
    @ValueSource(strings = ["some string"])
    @NullAndEmptySource
    fun `add works if elements number exceed expectedSize`(secondElement: String?) {
        val set = newCompactSet<String?>(1)
        set.add("1")
        set.add(secondElement)
        Assertions.assertEquals(2, set.size)
        Assertions.assertTrue(set.contains("1"))
        Assertions.assertTrue(set.contains(secondElement))
    }

    @Test
    fun `correctly works when expectedSize is 0`() {
        val set = newCompactSet<String>(0)
        set.add("1")
        Assertions.assertEquals(1, set.size)
        Assertions.assertTrue(set.contains("1"))
    }

    @Test
    fun `correctly adds in case of collision`() {
        val a = CollisionTest(value = "object", hash = 1)
        val b = CollisionTest(value = "another object with same hash", hash = 1)
        
        val set = newCompactSet<CollisionTest>()
        set.add(a)
        set.add(b)
        Assertions.assertEquals(2, set.size)
        Assertions.assertTrue(set.contains(a))
        Assertions.assertTrue(set.contains(b))
    }

    @Test
    fun `adds up to expectedSize in case of collision`() {
        val size = 10
        val set = newCompactSet<CollisionTest>(size)
        val elements = Array(size) { CollisionTest("obj_$it", hash = 1) }

        for (element in elements) {
            set.add(element)
        }
        Assertions.assertEquals(size, set.size)
        for (element in elements) {
            Assertions.assertTrue(set.contains(element))
        }
    }

    @Suppress("EqualsOrHashCode")
    private data class CollisionTest(private val value: String, private val hash: Int) {
        override fun hashCode(): Int {
            return hash
        }
    }
}