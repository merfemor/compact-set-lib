package ru.merfemor.compactset

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CompactSetFactoryTest {
    @Test
    fun `throws on incorrect expectedSize`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            newCompactSet<String>(-1)
        }
    }
}