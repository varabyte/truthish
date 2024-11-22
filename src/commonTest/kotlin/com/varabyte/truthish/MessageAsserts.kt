package com.varabyte.truthish

import com.varabyte.truthish.failure.ReportError
import com.varabyte.truthish.failure.assertSubstrings
import com.varabyte.truthish.failure.withMessage
import com.varabyte.truthish.subjects.*
import kotlin.test.Test

const val TEST_MESSAGE = "Your message here"

class MessageAsserts {
    class Stub

    class IntValue(val value: Int) : Comparable<IntValue> {
        override fun compareTo(other: IntValue): Int {
            return value.compareTo(other.value)
        }
    }

    @Test
    fun messageChecks() {
        val stub: Stub? = Stub()

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(stub).isNull()
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(stub!!).isNotInstanceOf<Stub>()
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(IntValue(3)).isGreaterThan(IntValue(4))
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(false).isTrue()
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(10).isGreaterThanEqual(100)
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(10.toByte()).isGreaterThanEqual(100.toByte())
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(10.toShort()).isGreaterThanEqual(100.toShort())
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(10.toLong()).isGreaterThanEqual(100.toLong())
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(10f).isGreaterThanEqual(100f)
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(10.0).isGreaterThanEqual(100.0)
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that("XYZ").isEmpty()
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(listOf(1, 2, 3)).containsAnyIn(listOf(4, 5, 6))
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(mapOf(1 to 1, 2 to 4, 3 to 9)).contains(4 to 16)
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(sequenceOf(1, 2, 3)).isEmpty()
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(arrayOf(1, 2, 3)).isEqualTo(arrayOf("a", "b", "c"))
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(booleanArrayOf(true, false)).isEqualTo(booleanArrayOf(false, true))
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(byteArrayOf(1, 2, 3)).isEqualTo(byteArrayOf(1, 2, 4))
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(charArrayOf('a', 'b', 'c')).isEqualTo(charArrayOf('a', 'b', 'd'))
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(shortArrayOf(1, 2, 3)).isEqualTo(shortArrayOf(1, 2, 4))
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(intArrayOf(1, 2, 3)).isEqualTo(intArrayOf(1, 2, 4))
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(longArrayOf(1, 2, 3)).isEqualTo(longArrayOf(1, 2, 4))
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(floatArrayOf(1f, 2f, 3f)).isEqualTo(floatArrayOf(1f, 2f, 4f))
        }.assertSubstrings(TEST_MESSAGE)

        assertThrows<ReportError> {
            assertWithMessage(TEST_MESSAGE).that(doubleArrayOf(1.0, 2.0, 3.0)).isEqualTo(doubleArrayOf(1.0, 2.0, 4.0))
        }.assertSubstrings(TEST_MESSAGE)
    }
}