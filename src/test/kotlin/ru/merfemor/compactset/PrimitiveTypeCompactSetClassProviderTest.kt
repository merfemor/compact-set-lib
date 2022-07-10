package ru.merfemor.compactset

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PrimitiveTypeCompactSetClassProviderTest {
    private val underTest = PrimitiveTypeCompactSetClassProvider()

    @Test
    fun `generates impl class for int`() {
        underTest.getImplClass(Integer::class.java)
    }

    @Test
    fun `returns same class when called twice with same type`() {
        val c1 = underTest.getImplClass(Integer::class.java)
        val c2 = underTest.getImplClass(Integer::class.java)
        Assertions.assertSame(c1, c2)
    }

    @Test
    fun `returns different classes for different types`() {
        val c1 = underTest.getImplClass(Integer::class.java)
        val c2 = underTest.getImplClass(Double::class.java)
        Assertions.assertNotSame(c1, c2)
    }
}