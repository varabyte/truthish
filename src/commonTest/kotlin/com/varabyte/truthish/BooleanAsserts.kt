package com.varabyte.truthish

import com.varabyte.truthish.failure.ReportError
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import com.varabyte.truthish.failure.withStrategy
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
            assertThrows<ReportError> {
                assertThat(true).isFalse()
            }.assertSubstrings(Summaries.EXPECTED_FALSE)

            assertThrows<ReportError> {
                assertThat(false).isTrue()
            }.assertSubstrings(Summaries.EXPECTED_TRUE)
        }
    }
}