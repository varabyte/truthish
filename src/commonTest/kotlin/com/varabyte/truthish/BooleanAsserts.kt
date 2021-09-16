package com.varabyte.truthish

import com.varabyte.truthish.failure.Summaries
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
            val testStrategy = TestStrategy()

            assertThat(true).withStrategy(testStrategy).isFalse()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_FALSE)
            assertThat(false).withStrategy(testStrategy).isTrue()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_TRUE)
        }
    }
}