package com.varabyte.truthish

import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
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
            assertThrows<AssertionError> {
                assertThat("ASDF").isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_EMPTY)
            assertThrows<AssertionError> {
                assertThat("???").isBlank()
            }.assertSubstrings(Summaries.EXPECTED_BLANK)
            assertThrows<AssertionError> {
                assertThat("").isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_NOT_EMPTY)
            assertThrows<AssertionError> {
                assertThat("       ").isNotBlank()
            }.assertSubstrings(Summaries.EXPECTED_NOT_BLANK)

            assertThrows<AssertionError> {
                assertThat("Hello World").hasLength(3)
            }.assertSubstrings(Summaries.EXPECTED_LENGTH, "3", "11", "\"Hello World\"")

            assertThrows<AssertionError> {
                assertThat("Hello World").startsWith("llo")
            }.assertSubstrings(Summaries.EXPECTED_STARTS_WITH)
            assertThrows<AssertionError> {
                assertThat("Hello World").endsWith("llo")
            }.assertSubstrings(Summaries.EXPECTED_ENDS_WITH)
            assertThrows<AssertionError> {
                assertThat("Hello World").doesNotStartWith("He")
            }.assertSubstrings(Summaries.EXPECTED_NOT_STARTS_WITH)
            assertThrows<AssertionError> {
                assertThat("Hello World").doesNotEndWith("ld")
            }.assertSubstrings(Summaries.EXPECTED_NOT_ENDS_WITH)

            assertThrows<AssertionError> {
                assertThat("Hello World").matches("ASDF")
            }.assertSubstrings(Summaries.EXPECTED_MATCH)
            assertThrows<AssertionError> {
                assertThat("Hello World").matches(Regex("[0-9]+"))
            }.assertSubstrings(Summaries.EXPECTED_MATCH)

            assertThrows<AssertionError> {
                assertThat("Hello World").doesNotMatch("..... .....")
            }.assertSubstrings(Summaries.EXPECTED_NOT_MATCH)
            assertThrows<AssertionError> {
                assertThat("Hello World").doesNotMatch(Regex(".+"))
            }.assertSubstrings(Summaries.EXPECTED_NOT_MATCH)

            assertThrows<AssertionError> {
                assertThat("Hello World").contains("Wello")
            }.assertSubstrings(Summaries.EXPECTED_CONTAINS)
            assertThrows<AssertionError> {
                assertThat("Hello World").containsMatch("AAA?A")
            }.assertSubstrings(Summaries.EXPECTED_CONTAINS)
            assertThrows<AssertionError> {
                assertThat("Hello World").containsMatch(Regex("12(34)"))
            }.assertSubstrings(Summaries.EXPECTED_CONTAINS)

            assertThrows<AssertionError> {
                assertThat("Hello World").doesNotContain("o Wo")
            }.assertSubstrings(Summaries.EXPECTED_NOT_CONTAINS)
            assertThrows<AssertionError> {
                assertThat("Hello World").doesNotContainMatch("l+")
            }.assertSubstrings(Summaries.EXPECTED_NOT_CONTAINS)
            assertThrows<AssertionError> {
                assertThat("Hello World").doesNotContainMatch(Regex("or."))
            }.assertSubstrings(Summaries.EXPECTED_NOT_CONTAINS)
        }
    }
}