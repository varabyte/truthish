package com.varabyte.truthish

import com.varabyte.truthish.failure.DetailsFor
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
            }.assertSubstrings(Summaries.EXPECTED_EMPTY, DetailsFor.VALUE, "\"ASDF\"")
            assertThrows<AssertionError> {
                assertThat("???").isBlank()
            }.assertSubstrings(Summaries.EXPECTED_BLANK, DetailsFor.VALUE, "\"???\"")
            assertThrows<AssertionError> {
                assertThat("").isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_NOT_EMPTY)
            assertThrows<AssertionError> {
                assertThat("       ").isNotBlank()
            }.assertSubstrings(Summaries.EXPECTED_NOT_BLANK, DetailsFor.VALUE, "\"       \"")

            assertThrows<AssertionError> {
                assertThat("Hello World").hasLength(3)
            }.assertSubstrings(Summaries.EXPECTED_LENGTH, "3", "11", "\"Hello World\"")

            assertThrows<AssertionError> {
                assertThat("Hello World").startsWith("llo")
            }.assertSubstrings(Summaries.EXPECTED_STARTS_WITH, "Expected to start with")
            assertThrows<AssertionError> {
                assertThat("Hello World").endsWith("llo")
            }.assertSubstrings(Summaries.EXPECTED_ENDS_WITH, "Expected to end with")
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
            }.assertSubstrings(Summaries.EXPECTED_NOT_CONTAINS, "Expected not to contain")
            assertThrows<AssertionError> {
                assertThat("Hello World").doesNotContainMatch("l+")
            }.assertSubstrings(Summaries.EXPECTED_NOT_CONTAINS, "Expected not to contain match")
            assertThrows<AssertionError> {
                assertThat("Hello World").doesNotContainMatch(Regex("or."))
            }.assertSubstrings(Summaries.EXPECTED_NOT_CONTAINS)
        }
    }

    @Test
    fun valuesThatCauseFailuresAreNeverTruncated() {
        val numbers = (1..100).joinToString(",")

        assertThrows<AssertionError> {
            assertThat("Hello World").contains(numbers)
        }.assertSubstrings(
            Summaries.EXPECTED_CONTAINS,
            "Expected to contain", "\"$numbers\"",
        )

        assertThrows<AssertionError> {
            assertThat("Hello World").matches(numbers)
        }.assertSubstrings(
            Summaries.EXPECTED_MATCH,
            "Expected to match", "\"$numbers\"",
        )
    }

    @Test
    fun longStringsAreTruncatedAroundWhatTheAssertionChecked() {
        val numbers = (1..100).joinToString(",")

        assertThrows<AssertionError> {
            assertThat(numbers).startsWith("x")
        }.assertSubstrings(
            Summaries.EXPECTED_STARTS_WITH,
            DetailsFor.BUT_WAS, "\"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,3\" … (191 more chars)",
        )

        assertThrows<AssertionError> {
            assertThat(numbers).endsWith("x")
        }.assertSubstrings(
            Summaries.EXPECTED_ENDS_WITH,
            DetailsFor.BUT_WAS, "… (191 more chars) \",68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100\"",
        )

        assertThrows<AssertionError> {
            assertThat(numbers).doesNotContain(",50,")
        }.assertSubstrings(
            Summaries.EXPECTED_NOT_CONTAINS,
            DetailsFor.BUT_WAS, "… (88 more chars) \"3,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66\" … (103 more chars)",
        )

        // The window is centered on the middle of the match (",50,…,70"), around "60", not on its start
        assertThrows<AssertionError> {
            assertThat(numbers).doesNotContainMatch(Regex(",50,.+,70"))
        }.assertSubstrings(
            Summaries.EXPECTED_NOT_CONTAINS,
            DetailsFor.BUT_WAS, "… (119 more chars) \",44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,\" … (72 more chars)",
        )

        // Nothing in particular to show, so both ends are kept
        assertThrows<AssertionError> {
            assertThat(numbers).contains("x")
        }.assertSubstrings(
            Summaries.EXPECTED_CONTAINS,
            DetailsFor.BUT_WAS, "\"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20\" … (191 more chars) \"4,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100\"",
        )
    }

    @Test
    fun longStringsAreTruncatedAroundTheFirstDifference() {
        val numbers = (1..100).joinToString(",")

        // One character replaced (the "5" of "50")
        assertThrows<AssertionError> {
            assertThat(numbers.replace(",50,", ",X0,")).isEqualTo(numbers)
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.EXPECTED, "… (88 more chars) \"3,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66\" … (103 more chars)",
            DetailsFor.BUT_WAS, "… (88 more chars) \"3,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,X0,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66\" … (103 more chars)",
        )

        // The strings differ only in length
        assertThrows<AssertionError> {
            assertThat((1..60).joinToString(",")).isEqualTo(numbers)
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.EXPECTED, "… (71 more chars) \",28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,\" … (120 more chars)",
            DetailsFor.BUT_WAS, "… (71 more chars) \",28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60\"",
        )

        // A string short enough to show whole
        assertThrows<AssertionError> {
            assertThat(numbers).isEqualTo("1,2,3")
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.EXPECTED, "\"1,2,3\"",
            DetailsFor.BUT_WAS, "\"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,3\" … (191 more chars)",
        )
    }

    @Test
    fun multiLineStringsAreShownAsADiff() {
        assertThrows<AssertionError> {
            assertThat("a\nb\nc").isEqualTo("a\nB\nc")
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            """
            Diff:   a
                  - B
                  + b
                    c
            """.trimIndent(),
        )

        // Only one side needs to span multiple lines to result in a diff
        run {
            assertThrows<AssertionError> {
                assertThat("a").isEqualTo("a\nb\nc")
            }.assertSubstrings(
                Summaries.EXPECTED_EQUAL,
                """
                Diff:   a
                      - b
                      - c
                """.trimIndent(),
            )
            assertThrows<AssertionError> {
                assertThat("a\nb\nc").isEqualTo("a")
            }.assertSubstrings(
                Summaries.EXPECTED_EQUAL,
                """
                Diff:   a
                      + b
                      + c
                """.trimIndent(),
            )
        }
    }

    @Test
    fun multiLineStringDiffIgnoresLineEndingsButNotesThem() {
        assertThrows<AssertionError> {
            assertThat("a\r\nb\r\nc").isEqualTo("a\nB\nc")
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            """
            Diff:   a
                  - B
                  + b
                    c
                  (line endings also differ)
            """.trimIndent(),
        )
    }

    @Test
    fun multiLineStringDiffNotesAMissingFinalNewline() {
        assertThrows<AssertionError> {
            assertThat("a\nb").isEqualTo("a\nB\n")
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            """
            Diff:   a
                  - B
                  + b
                  (expected ends with a newline but actual doesn't)
            """.trimIndent(),
        )

        assertThrows<AssertionError> {
            assertThat("a\nb\n").isEqualTo("a\nB")
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            """
            Diff:   a
                  - B
                  + b
                  (actual ends with a newline but expected doesn't)
            """.trimIndent(),
        )

        // The final newline can be the only difference
        assertThrows<AssertionError> {
            assertThat("a\nb").isEqualTo("a\nb\n")
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            """
            Diff:   a
                    b
                  (expected ends with a newline but actual doesn't)
            """.trimIndent(),
        )
    }

    @Test
    fun multiLineStringDiffCanHaveSeveralNotes() {
        assertThrows<AssertionError> {
            assertThat("a\r\nb").isEqualTo("a\nB\n")
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            """
            Diff:   a
                  - B
                  + b
                  (line endings also differ; expected ends with a newline but actual doesn't)
            """.trimIndent(),
        )
    }

    @Test
    fun multiLineStringsThatOnlyDifferInLineEndingsAreNotDiffed() {
        assertThrows<AssertionError> {
            assertThat("a\r\nb").isEqualTo("a\nb")
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.EXPECTED, "\"a\\nb\"",
            DetailsFor.BUT_WAS, "\"a\\r\\nb\"",
        )
        // Note that we intentionally don't show a note about different line endings here, because they are easy to
        // see at a glance from the report:
        // Expected: "a\nb"
        // But was : "a\r\nb"
    }
}
