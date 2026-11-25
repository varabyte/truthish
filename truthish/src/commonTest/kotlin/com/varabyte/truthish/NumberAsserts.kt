package com.varabyte.truthish

import com.varabyte.truthish.failure.DetailsFor
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

            assertThat(5).isIn(1..10)
            assertThat(5).isIn(5..5)
            assertThat(5).isNotIn(6..10)
            @Suppress("EmptyRange") // An empty range contains nothing
            assertThat(5).isNotIn(10..1)
        }

        run {
            // Test false statements
            assertThrows<AssertionError> {
                assertThat(5).isGreaterThanEqual(6)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "6", "5")
            assertThrows<AssertionError> {
                assertThat(5).isGreaterThan(5)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "5")
            assertThrows<AssertionError> {
                assertThat(5).isLessThanEqual(4)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "4", "5")
            assertThrows<AssertionError> {
                assertThat(5).isLessThan(3)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "3", "5")

            // Ranges show their bounds, not every element in them
            assertThrows<AssertionError> {
                assertThat(5).isIn(6..10)
            }.assertSubstrings(Summaries.EXPECTED_IN_RANGE, "Expected in", "6..10", DetailsFor.BUT_WAS, "5")
            assertThrows<AssertionError> {
                assertThat(5).isNotIn(1..10)
            }.assertSubstrings(Summaries.EXPECTED_NOT_IN_RANGE, "Expected not in", "1..10", DetailsFor.BUT_WAS, "5")
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
        assertThat(5.toByte()).isIn(1..10)
        assertThat(5.toByte()).isNotIn(6..10)

        assertThrows<AssertionError> {
            assertThat(5.toByte()).isIn(6..10)
        }.assertSubstrings(Summaries.EXPECTED_IN_RANGE, "6..10", DetailsFor.BUT_WAS, "5")
        assertThrows<AssertionError> {
            assertWithMessage(TEST_MESSAGE).that(5.toByte()).isNotIn(1..10)
        }.assertSubstrings(Summaries.EXPECTED_NOT_IN_RANGE, DetailsFor.MESSAGE, TEST_MESSAGE, "1..10")
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
        assertThat(5.toShort()).isIn(1..10)
        assertThat(5.toShort()).isNotIn(6..10)

        assertThrows<AssertionError> {
            assertThat(5.toShort()).isIn(6..10)
        }.assertSubstrings(Summaries.EXPECTED_IN_RANGE, "6..10", DetailsFor.BUT_WAS, "5")
        assertThrows<AssertionError> {
            assertWithMessage(TEST_MESSAGE).that(5.toShort()).isNotIn(1..10)
        }.assertSubstrings(Summaries.EXPECTED_NOT_IN_RANGE, DetailsFor.MESSAGE, TEST_MESSAGE, "1..10")
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
        assertThat(5.toLong()).isIn(4.toLong()..6.toLong())
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

            assertThat(5.3f).isIn(5.25f..5.5f)
            // NaN is in no range
            assertThat(Float.NaN).isNotIn(5.25f..5.5f)

            assertThat(Float.NaN).isNaN()
            assertThat(5.5f).isNotNaN()
            assertThat(5.5f).isFinite()
            assertThat(Float.POSITIVE_INFINITY).isPositiveInfinity()
            assertThat(Float.NEGATIVE_INFINITY).isNegativeInfinity()
            assertThat(0f).isZero()
            assertThat(-0f).isZero()
            assertThat(5.5f).isNonZero()
            assertThat(Float.NaN).isNonZero()
        }

        run {
            // Test false statements
            // NOTE: Special values chosen to ensure string output is the same on all platforms. If you change, be sure to
            // retest all targets.
            assertThrows<AssertionError> {
                assertThat(5.25f).isNotWithin(0.5f).of(5.5f)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "0.5", "5.5", "5.25")
            assertThrows<AssertionError> {
                assertThat(5.25f).isWithin(0.125f).of(5.5f)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "0.125", "5.5", "5.25")

            // NaN is never within (or not within) any distance of anything
            assertThrows<AssertionError> {
                assertThat(Float.NaN).isWithin(0.1f).of(5.5f)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "NaN")
            assertThrows<AssertionError> {
                assertThat(5.3f).isWithin(0.1f).of(Float.NaN)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "NaN")
            assertThrows<AssertionError> {
                assertThat(Float.NaN).isNotWithin(0.1f).of(5.5f)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "NaN")
            assertThrows<AssertionError> {
                assertThat(5.3f).isNotWithin(0.1f).of(Float.NaN)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "NaN")

            assertThrows<AssertionError> {
                assertThat(5.5f).isNaN()
            }.assertSubstrings(Summaries.EXPECTED_NAN, DetailsFor.VALUE, "5.5")
            assertThrows<AssertionError> {
                assertThat(Float.NaN).isNotNaN()
            }.assertSubstrings(Summaries.EXPECTED_NOT_NAN)
            assertThrows<AssertionError> {
                assertThat(Float.NaN).isFinite()
            }.assertSubstrings(Summaries.EXPECTED_FINITE, DetailsFor.VALUE, "NaN")
            assertThrows<AssertionError> {
                assertThat(Float.NEGATIVE_INFINITY).isFinite()
            }.assertSubstrings(Summaries.EXPECTED_FINITE, DetailsFor.VALUE, "Infinity")
            assertThrows<AssertionError> {
                assertThat(Float.NEGATIVE_INFINITY).isPositiveInfinity()
            }.assertSubstrings(Summaries.EXPECTED_POSITIVE_INFINITY, DetailsFor.VALUE, "Infinity")
            assertThrows<AssertionError> {
                assertThat(5.5f).isNegativeInfinity()
            }.assertSubstrings(Summaries.EXPECTED_NEGATIVE_INFINITY, DetailsFor.VALUE, "5.5")
            assertThrows<AssertionError> {
                assertThat(5.5f).isZero()
            }.assertSubstrings(Summaries.EXPECTED_ZERO, DetailsFor.VALUE, "5.5")
            assertThrows<AssertionError> {
                assertThat(-0f).isNonZero()
            }.assertSubstrings(Summaries.EXPECTED_NON_ZERO)
            // Unlike isZero, isEqualTo tells zero and negative zero apart
            assertThrows<AssertionError> {
                assertThat(-0f).isEqualTo(0f)
            }.assertSubstrings(Summaries.EXPECTED_EQUAL)
        }

        run {
            // Test invalid epsilons
            assertThrows<IllegalArgumentException> {
                assertThat(5.3f).isWithin(-0.1f)
            }
            assertThrows<IllegalArgumentException> {
                assertThat(5.3f).isNotWithin(Float.NaN)
            }
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

            assertThat(5.3).isIn(5.25..5.5)
            // NaN is in no range
            assertThat(Double.NaN).isNotIn(5.25..5.5)

            assertThat(Double.NaN).isNaN()
            assertThat(5.5).isNotNaN()
            assertThat(5.5).isFinite()
            assertThat(Double.POSITIVE_INFINITY).isPositiveInfinity()
            assertThat(Double.NEGATIVE_INFINITY).isNegativeInfinity()
            assertThat(0.0).isZero()
            assertThat(-0.0).isZero()
            assertThat(5.5).isNonZero()
            assertThat(Double.NaN).isNonZero()
        }

        run {
            // Test false statements
            assertThrows<AssertionError> {
                assertThat(5.3).isNotWithin(0.5).of(5.5)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "0.5", "5.5", "5.3")
            assertThrows<AssertionError> {
                assertThat(5.3).isWithin(0.1).of(5.5)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "0.1", "5.5", "5.3")

            // NaN is never within (or not within) any distance of anything
            assertThrows<AssertionError> {
                assertThat(Double.NaN).isWithin(0.1).of(5.5)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "NaN")
            assertThrows<AssertionError> {
                assertThat(5.3).isWithin(0.1).of(Double.NaN)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "NaN")
            assertThrows<AssertionError> {
                assertThat(Double.NaN).isNotWithin(0.1).of(5.5)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "NaN")
            assertThrows<AssertionError> {
                assertThat(5.3).isNotWithin(0.1).of(Double.NaN)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON, "NaN")
            assertThrows<AssertionError> {
                assertThat(Double.NaN).isIn(5.25..5.5)
            }.assertSubstrings(Summaries.EXPECTED_IN_RANGE, "5.25..5.5", "NaN")

            assertThrows<AssertionError> {
                assertThat(5.5).isNaN()
            }.assertSubstrings(Summaries.EXPECTED_NAN, DetailsFor.VALUE, "5.5")
            assertThrows<AssertionError> {
                assertThat(Double.NaN).isNotNaN()
            }.assertSubstrings(Summaries.EXPECTED_NOT_NAN)
            assertThrows<AssertionError> {
                assertThat(Double.NaN).isFinite()
            }.assertSubstrings(Summaries.EXPECTED_FINITE, DetailsFor.VALUE, "NaN")
            assertThrows<AssertionError> {
                assertThat(Double.NEGATIVE_INFINITY).isFinite()
            }.assertSubstrings(Summaries.EXPECTED_FINITE, DetailsFor.VALUE, "Infinity")
            assertThrows<AssertionError> {
                assertThat(Double.NEGATIVE_INFINITY).isPositiveInfinity()
            }.assertSubstrings(Summaries.EXPECTED_POSITIVE_INFINITY, DetailsFor.VALUE, "Infinity")
            assertThrows<AssertionError> {
                assertThat(5.5).isNegativeInfinity()
            }.assertSubstrings(Summaries.EXPECTED_NEGATIVE_INFINITY, DetailsFor.VALUE, "5.5")
            assertThrows<AssertionError> {
                assertThat(5.5).isZero()
            }.assertSubstrings(Summaries.EXPECTED_ZERO, DetailsFor.VALUE, "5.5")
            assertThrows<AssertionError> {
                assertThat(-0.0).isNonZero()
            }.assertSubstrings(Summaries.EXPECTED_NON_ZERO)
            // Unlike isZero, isEqualTo tells zero and negative zero apart
            assertThrows<AssertionError> {
                assertThat(-0.0).isEqualTo(0.0)
            }.assertSubstrings(Summaries.EXPECTED_EQUAL)
        }

        run {
            // Test invalid epsilons
            assertThrows<IllegalArgumentException> {
                assertThat(5.3).isWithin(-0.1)
            }
            assertThrows<IllegalArgumentException> {
                assertThat(5.3).isNotWithin(Double.NaN)
            }
        }
    }

}