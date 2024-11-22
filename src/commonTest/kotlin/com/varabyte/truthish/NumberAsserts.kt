package com.varabyte.truthish

import com.varabyte.truthish.failure.ReportError
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import kotlin.test.Test

class NumberAssets {
    @Test
    fun intChecks() {
        run {
            // Test true statements
            assertThat(5).isEqualTo(5)
            assertThat(5).isNotEqualTo(6)
            assertThat(5).isGreaterThanEqual(5)
            assertThat(5).isGreaterThanEqual(4)
            assertThat(5).isGreaterThan(4)
            assertThat(5).isLessThanEqual(5)
            assertThat(5).isLessThanEqual(6)
            assertThat(5).isLessThan(6)
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(5).isGreaterThanEqual(6)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "5", "6")
            assertThrows<ReportError> {
                assertThat(5).isGreaterThan(5)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "5")
            assertThrows<ReportError> {
                assertThat(5).isLessThanEqual(4)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "5", "4")
            assertThrows<ReportError> {
                assertThat(5).isLessThan(3)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "5", "3")
        }
    }

    @Test
    fun byteChecks() {
        assertThat(5.toByte()).isEqualTo(5.toByte())
        assertThat(5.toByte()).isNotEqualTo(6.toByte())
        assertThat(5.toByte()).isGreaterThanEqual(5.toByte())
        assertThat(5.toByte()).isGreaterThanEqual(4.toByte())
        assertThat(5.toByte()).isGreaterThan(4.toByte())
        assertThat(5.toByte()).isLessThanEqual(5.toByte())
        assertThat(5.toByte()).isLessThanEqual(6.toByte())
        assertThat(5.toByte()).isLessThan(6.toByte())
    }

    @Test
    fun shortChecks() {
        assertThat(5.toShort()).isEqualTo(5.toShort())
        assertThat(5.toShort()).isNotEqualTo(6.toShort())
        assertThat(5.toShort()).isGreaterThanEqual(5.toShort())
        assertThat(5.toShort()).isGreaterThanEqual(4.toShort())
        assertThat(5.toShort()).isGreaterThan(4.toShort())
        assertThat(5.toShort()).isLessThanEqual(5.toShort())
        assertThat(5.toShort()).isLessThanEqual(6.toShort())
        assertThat(5.toShort()).isLessThan(6.toShort())
    }


    @Test
    fun longChecks() {
        assertThat(5.toLong()).isEqualTo(5.toLong())
        assertThat(5.toLong()).isNotEqualTo(6.toLong())
        assertThat(5.toLong()).isGreaterThanEqual(5.toLong())
        assertThat(5.toLong()).isGreaterThanEqual(4.toLong())
        assertThat(5.toLong()).isGreaterThan(4.toLong())
        assertThat(5.toLong()).isLessThanEqual(5.toLong())
        assertThat(5.toLong()).isLessThanEqual(6.toLong())
        assertThat(5.toLong()).isLessThan(6.toLong())
    }

    @Test
    fun floatChecks() {
        run {
            // Test true statements
            assertThat(5f).isEqualTo(5f)
            assertThat(5f).isNotEqualTo(6f)
            assertThat(5f).isGreaterThanEqual(5f)
            assertThat(5f).isGreaterThanEqual(4f)
            assertThat(5f).isGreaterThan(4f)
            assertThat(5f).isLessThanEqual(5f)
            assertThat(5f).isLessThanEqual(6f)
            assertThat(5f).isLessThan(6f)

            assertThat(5.3f).isWithin(0.5f).of(5.5f)
            assertThat(5.3f).isNotWithin(0.1f).of(5.5f)
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(5.3f).isNotWithin(0.5f).of(5.5f)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "5.3", "0.5", "5.5")
            assertThrows<ReportError> {
                assertThat(5.3f).isWithin(0.1f).of(5.5f)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "5.3", "0.1", "5.5")
        }
    }

    @Test
    fun doubleChecks() {
        run {
            // Test true statements
            assertThat(5.0).isEqualTo(5.0)
            assertThat(5.0).isNotEqualTo(6.0)
            assertThat(5.0).isGreaterThanEqual(5.0)
            assertThat(5.0).isGreaterThanEqual(4.0)
            assertThat(5.0).isGreaterThan(4.0)
            assertThat(5.0).isLessThanEqual(5.0)
            assertThat(5.0).isLessThanEqual(6.0)
            assertThat(5.0).isLessThan(6.0)

            assertThat(5.3).isWithin(0.5).of(5.5)
            assertThat(5.3).isNotWithin(0.1).of(5.5)
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(5.3).isNotWithin(0.5).of(5.5)
            }
            assertThrows<ReportError> {
                assertThat(5.3).isWithin(0.1).of(5.5)
            }
        }
    }

}