package com.varabyte.truthish

import com.varabyte.truthish.failure.ReportError
import com.varabyte.truthish.failure.assertSubstrings
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
    }
}