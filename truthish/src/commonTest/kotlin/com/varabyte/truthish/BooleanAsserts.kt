package com.varabyte.truthish

import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import kotlin.test.Test

class BooleanAsserts {
    @Test
    fun boolChecks() {
        run {
            // Test true statements
            assertThat(true).isEqualTo(true)
            assertThat(true).isNotEqualTo(false)

            assertThat(true).isTrue()
            assertThat(false).isFalse()
        }

        run {
            // Test false statements
            assertThrows<AssertionError> {
                assertThat(true).isFalse()
            }.assertSubstrings(Summaries.EXPECTED_FALSE)

            assertThrows<AssertionError> {
                assertThat(false).isTrue()
            }.assertSubstrings(Summaries.EXPECTED_TRUE)
        }
    }
}