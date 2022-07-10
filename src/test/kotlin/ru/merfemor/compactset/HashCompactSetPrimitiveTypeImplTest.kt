package ru.merfemor.compactset

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class HashCompactSetPrimitiveTypeImplTest {
    @ParameterizedTest
    @MethodSource("getTestEmptyArgs")
    fun `initial size is zero`(arg: CompactSetTestEmptyArgs<*>) {
        val set = arg.newCompactSet()
        Assertions.assertEquals(0, set.size)
    }

    @ParameterizedTest
    @MethodSource("getTestOneValueArgs")
    fun <T> `contains on empty set is false`(arg: CompactSetTestOneValueArgs<T>) {
        val set = arg.newCompactSet()
        Assertions.assertFalse(set.contains(arg.value))
    }

    @ParameterizedTest
    @MethodSource("getTestOneValueArgs")
    fun <T> `add returns true when element was not in set`(arg: CompactSetTestOneValueArgs<T>) {
        val set = arg.newCompactSet()
        Assertions.assertTrue(set.add(arg.value))
    }

    @ParameterizedTest
    @MethodSource("getTestOneValueArgs")
    fun <T> `size changes after add`(arg: CompactSetTestOneValueArgs<T>) {
        val set = arg.newCompactSet()
        set.add(arg.value)
        Assertions.assertEquals(1, set.size)
    }

    @ParameterizedTest
    @MethodSource("getTestOneValueArgs")
    fun <T> `contains returns true after add`(arg: CompactSetTestOneValueArgs<T>) {
        val set = arg.newCompactSet()
        set.add(arg.value)
        Assertions.assertTrue(set.contains(arg.value))
    }

    @ParameterizedTest
    @MethodSource("getTestOneValueArgs")
    fun <T> `duplicate element is not added`(arg: CompactSetTestOneValueArgs<T>) {
        val set = arg.newCompactSet()
        set.add(arg.value)
        Assertions.assertFalse(set.add(arg.value))
        Assertions.assertEquals(1, set.size)
    }

    @ParameterizedTest
    @MethodSource("getTestTwoValueArgs")
    fun <T> `add works if elements number exceed expectedSize`(arg: CompactSetTestTwoValueArgs<T>) {
        val set = arg.newCompactSet(1)
        set.add(arg.firstValue)
        set.add(arg.secondValue)
        Assertions.assertEquals(2, set.size)
        Assertions.assertTrue(set.contains(arg.firstValue))
        Assertions.assertTrue(set.contains(arg.secondValue))
    }

    @ParameterizedTest
    @MethodSource("getTestOneValueArgs")
    fun <T> `correctly works when expectedSize is 0`(arg: CompactSetTestOneValueArgs<T>) {
        val set = arg.newCompactSet(0)
        set.add(arg.value)
        Assertions.assertEquals(1, set.size)
        Assertions.assertTrue(set.contains(arg.value))
    }

    @Test
    fun `correctly adds in case of collision`() {
        val capacity = 10
        val actualCapacity = (capacity / AbstractHashCompactSet.LOAD_FACTOR).toInt() + 1
        val set = newCompactSet<Int>(capacity)

        val a = 5
        val b = a + actualCapacity
        Assertions.assertTrue(set.add(a))
        Assertions.assertTrue(set.add(b))
        Assertions.assertEquals(2, set.size)
        Assertions.assertTrue(set.contains(a))
        Assertions.assertTrue(set.contains(b))
    }

    companion object {
        @JvmStatic
        private fun getTestEmptyArgs(): Stream<CompactSetTestEmptyArgs<*>> {
            return Stream.of(
                CompactSetTestEmptyArgs.create<Int>(),
                CompactSetTestEmptyArgs.create<Double>(),
                CompactSetTestEmptyArgs.create<Long>()
            )
        }

        @JvmStatic
        private fun getTestOneValueArgs(): Stream<CompactSetTestOneValueArgs<*>> {
            return Stream.of(
                CompactSetTestOneValueArgs.create(0),
                CompactSetTestOneValueArgs.create(-10),
                CompactSetTestOneValueArgs.create(12),
                CompactSetTestOneValueArgs.create(Int.MAX_VALUE),
                CompactSetTestOneValueArgs.create(Int.MIN_VALUE),
                CompactSetTestOneValueArgs.create<Int?>(null),

                CompactSetTestOneValueArgs.create(0L),
                CompactSetTestOneValueArgs.create(-10L),
                CompactSetTestOneValueArgs.create(12L),
                CompactSetTestOneValueArgs.create(Long.MIN_VALUE),
                CompactSetTestOneValueArgs.create(Long.MAX_VALUE),
                CompactSetTestOneValueArgs.create<Long?>(null),

                CompactSetTestOneValueArgs.create(0.0),
                CompactSetTestOneValueArgs.create(-10.0),
                CompactSetTestOneValueArgs.create(12.0),
                CompactSetTestOneValueArgs.create(Double.NaN),
                CompactSetTestOneValueArgs.create(Double.MIN_VALUE),
                CompactSetTestOneValueArgs.create(Double.MAX_VALUE),
                CompactSetTestOneValueArgs.create(Double.POSITIVE_INFINITY),
                CompactSetTestOneValueArgs.create(Double.NEGATIVE_INFINITY),
                CompactSetTestOneValueArgs.create<Double?>(null),
            )
        }

        @JvmStatic
        private fun getTestTwoValueArgs(): Stream<CompactSetTestTwoValueArgs<*>> {
            return Stream.of(
                CompactSetTestTwoValueArgs.create(10, 0),
                CompactSetTestTwoValueArgs.create(10, 12),
                CompactSetTestTwoValueArgs.create(10, Int.MAX_VALUE),
                CompactSetTestTwoValueArgs.create(10, Int.MIN_VALUE),
                CompactSetTestTwoValueArgs.create(10, null),

                CompactSetTestTwoValueArgs.create(10L, 0L),
                CompactSetTestTwoValueArgs.create(10L, 12L),
                CompactSetTestTwoValueArgs.create(10L, Long.MIN_VALUE),
                CompactSetTestTwoValueArgs.create(10L, Long.MAX_VALUE),
                CompactSetTestTwoValueArgs.create(10L, null),

                CompactSetTestTwoValueArgs.create(10.0, 0.0),
                CompactSetTestTwoValueArgs.create(10.0, 12.0),
                CompactSetTestTwoValueArgs.create(10.0, Double.NaN),
                CompactSetTestTwoValueArgs.create(10.0, Double.MIN_VALUE),
                CompactSetTestTwoValueArgs.create(10.0, Double.MAX_VALUE),
                CompactSetTestTwoValueArgs.create(10.0, Double.POSITIVE_INFINITY),
                CompactSetTestTwoValueArgs.create(10.0, Double.NEGATIVE_INFINITY),
                CompactSetTestTwoValueArgs.create(10.0, null),
            )
        }
    }
}