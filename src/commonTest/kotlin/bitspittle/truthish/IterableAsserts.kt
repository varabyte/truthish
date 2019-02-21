package bitspittle.truthish

import bitspittle.truthish.failure.withStrategy
import kotlin.test.Test

class IterableAsserts {
    @Test
    fun collectionChecks() {
        run {
            // Test true statements
            assertThat(listOf("a", "b", "c")).isEqualTo(listOf("a", "b", "c"))
            assertThat(listOf("a", "b", "c")).isNotEqualTo(listOf("a", "b", "d"))
        }

        run {
            // Test false statements
            val testStrategy = TestStrategy()

            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).isNotEqualTo(listOf("a", "b", "c"))
            testStrategy.verifyFailureAndClear("[ a, b, c ]")
        }
    }
}