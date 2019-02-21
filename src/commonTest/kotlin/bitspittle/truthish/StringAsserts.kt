package bitspittle.truthish

import bitspittle.truthish.failure.Summaries
import bitspittle.truthish.failure.withStrategy
import kotlin.test.Test

class StringAsserts {
    @Test
    fun stringChecks() {
        run {
            // Test true statements
            assertThat("").isEmpty()
            assertThat("  ").isBlank()
            assertThat("  ").isNotEmpty()
            assertThat("Hello World").isNotEmpty()
            assertThat("Hello World").isNotBlank()

            assertThat("").hasLength(0)
            assertThat("  ").hasLength(2)
            assertThat("Hello World").hasLength(11)

            assertThat("Hello World").startsWith("Hell")
            assertThat("Hello World").endsWith("orld")
            assertThat("Hello World").doesNotStartWith("orld")
            assertThat("Hello World").doesNotEndWith("Hell")

            assertThat("Hello World").matches("He.+ ...ld")
            assertThat("Hello World").matches(Regex(".+"))

            assertThat("Hello World").doesNotMatch("Goodbye")
            assertThat("Hello World").doesNotMatch(Regex("A"))

            assertThat("Hello World").contains("ello")
            assertThat("Hello World").containsMatch("elll?o")
            assertThat("Hello World").containsMatch(Regex("l.+ W"))

            assertThat("Hello World").doesNotContain("Jello")
            assertThat("Hello World").doesNotContainMatch("0+")
            assertThat("Hello World").doesNotContainMatch(Regex("[0-9]"))
        }

        run {
            // Test false statements
            val testStrategy = TestStrategy()

            assertThat("ASDF").withStrategy(testStrategy).isEmpty()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_EMPTY)
            assertThat("???").withStrategy(testStrategy).isBlank()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_BLANK)
            assertThat("").withStrategy(testStrategy).isNotEmpty()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_EMPTY)
            assertThat("       ").withStrategy(testStrategy).isNotBlank()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_BLANK)

            assertThat("Hello World").withStrategy(testStrategy).hasLength(3)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COMPARISON)

            assertThat("Hello World").withStrategy(testStrategy).startsWith("llo")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_STARTS_WITH)
            assertThat("Hello World").withStrategy(testStrategy).endsWith("llo")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_ENDS_WITH)
            assertThat("Hello World").withStrategy(testStrategy).doesNotStartWith("He")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_STARTS_WITH)
            assertThat("Hello World").withStrategy(testStrategy).doesNotEndWith("ld")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_ENDS_WITH)

            assertThat("Hello World").withStrategy(testStrategy).matches("ASDF")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_MATCH)
            assertThat("Hello World").withStrategy(testStrategy).matches(Regex("[0-9]+"))
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_MATCH)

            assertThat("Hello World").withStrategy(testStrategy).doesNotMatch("..... .....")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_MATCH)
            assertThat("Hello World").withStrategy(testStrategy).doesNotMatch(Regex(".+"))
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_MATCH)

            assertThat("Hello World").withStrategy(testStrategy).contains("Wello")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_CONTAINS)
            assertThat("Hello World").withStrategy(testStrategy).containsMatch("AAA?A")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_CONTAINS)
            assertThat("Hello World").withStrategy(testStrategy).containsMatch(Regex("12(34)"))
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_CONTAINS)

            assertThat("Hello World").withStrategy(testStrategy).doesNotContain("o Wo")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_CONTAINS)
            assertThat("Hello World").withStrategy(testStrategy).doesNotContainMatch("l+")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_CONTAINS)
            assertThat("Hello World").withStrategy(testStrategy).doesNotContainMatch(Regex("or."))
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_CONTAINS)
        }
    }
}