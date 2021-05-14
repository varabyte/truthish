package com.github.bitspittle.truthish

import com.github.bitspittle.truthish.failure.Summaries
import com.github.bitspittle.truthish.failure.withStrategy
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