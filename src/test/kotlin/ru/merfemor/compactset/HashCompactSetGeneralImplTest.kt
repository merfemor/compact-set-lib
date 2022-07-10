package ru.merfemor.compactset

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class HashCompactSetGeneralImplTest {

    @Test
    fun `throws on incorrect expectedSize`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            newCompactSet<String>(-1)
        }
    }

    @Test
    fun `initial size is zero`() {
        val set = newCompactSet<String>()
        Assertions.assertEquals(0, set.size)
    }

    @Test
    fun `contains on empty set is false`() {
        val set = newCompactSet<String>()
        Assertions.assertFalse(set.contains("some string"))
    }

    @Test
    fun `contains null on empty set is false`() {
        val set = newCompactSet<String?>()
        Assertions.assertFalse(set.contains(null))
    }

    @Test
    fun `add returns true when elements was not in set`() {
        val set = newCompactSet<String>()
        Assertions.assertTrue(set.add("element"))
    }

    @Test
    fun `size changes after add`() {
        val set = newCompactSet<String>()
        set.add("element")
        Assertions.assertEquals(1, set.size)
    }

    @Test
    fun `duplicate element is not added`() {
        val set = newCompactSet<String>()
        set.add("element")
        Assertions.assertFalse(set.add("element"))
        Assertions.assertEquals(1, set.size)
    }

    @Test
    fun `add and contains work correctly with null values`() {
        val set = newCompactSet<String?>()
        set.add(null)
        Assertions.assertEquals(1, set.size)
        Assertions.assertTrue(set.contains(null))
    }

    @Test
    fun `null is not added twice`() {
        val set = newCompactSet<String?>()
        set.add(null)
        Assertions.assertFalse(set.add(null))
        Assertions.assertEquals(1, set.size)
    }

    @Test
    fun `add works if elements number exceed expectedSize`() {
        val set = newCompactSet<String>(1)
        set.add("1")
        set.add("2")
        Assertions.assertEquals(2, set.size)
        Assertions.assertTrue(set.contains("1"))
        Assertions.assertTrue(set.contains("2"))
    }

    @Test
    fun `nulls not lost if added more than expectedSize`() {
        val set = newCompactSet<String?>(1)
        set.add(null)
        set.add("1")
        Assertions.assertEquals(2, set.size)
        Assertions.assertTrue(set.contains("1"))
        Assertions.assertTrue(set.contains(null))
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