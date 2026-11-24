package com.varabyte.truthish

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.MultipleReportsError
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import com.varabyte.truthish.failure.withMessage
import com.varabyte.truthish.subjects.comparingElementsUsing
import com.varabyte.truthish.subjects.containsAllIn
import com.varabyte.truthish.subjects.containsAnyIn
import com.varabyte.truthish.subjects.containsExactly
import com.varabyte.truthish.subjects.containsNoneIn
import kotlin.test.Test

class CorrespondenceAsserts {
    private val errors = listOf("Unresolved reference 'foo'", "'val' cannot be reassigned")

    @Test
    fun correspondenceChecks() {
        val subject = assertThat(errors).comparingElementsUsing(Correspondence.contains)
        run {
            // Test true statements
            subject.contains("cannot be reassigned")
            subject.doesNotContain("Type mismatch")
            subject.containsAnyIn("Type mismatch", "cannot be reassigned")
            subject.containsAllIn("cannot be reassigned")
            subject.containsAllIn("Unresolved reference", "cannot be reassigned").inOrder()
            subject.containsNoneIn("Type mismatch", "Unused variable")
            subject.containsExactly("cannot be reassigned", "Unresolved reference")
            subject.containsExactly("Unresolved reference", "cannot be reassigned").inOrder()
            subject.containsAnyIn(arrayOf("Type mismatch", "cannot be reassigned"))
            subject.containsAllIn(arrayOf("Unresolved reference", "cannot be reassigned")).inOrder()
            subject.containsNoneIn(arrayOf("Type mismatch", "Unused variable"))
            subject.containsExactly(arrayOf("Unresolved reference", "cannot be reassigned")).inOrder()
        }

        run {
            // Test false statements
            assertThrows<AssertionError> {
                subject.contains("Unused variable")
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_CONTAINS,
                "Expected to contain", "\"Unused variable\"",
                DetailsFor.BUT_WAS, "[ \"Unresolved reference 'foo'\", \"'val' cannot be reassigned\" ]",
                DetailsFor.TESTING_WHETHER, "actual element contains expected element",
            )

            assertThrows<AssertionError> {
                subject.doesNotContain("cannot be reassigned")
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                "Expected not to contain", "\"cannot be reassigned\"",
                DetailsFor.TESTING_WHETHER, "actual element contains expected element",
            )

            assertThrows<AssertionError> {
                subject.containsAnyIn("Unused variable", "Type mismatch")
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_CONTAINS,
                "Expected at least one element from", "[ \"Unused variable\", \"Type mismatch\" ]",
                DetailsFor.TESTING_WHETHER, "actual element contains expected element",
            )

            assertThrows<AssertionError> {
                subject.containsAllIn("cannot be reassigned", "Unused variable")
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_CONTAINS,
                DetailsFor.TESTING_WHETHER, "actual element contains expected element",
                DetailsFor.MISSING, "[ \"Unused variable\" ]",
            )

            assertThrows<AssertionError> {
                subject.containsAllIn("cannot be reassigned", "Unresolved reference")
                    .inOrder()
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_ORDERED,
                DetailsFor.TESTING_WHETHER, "actual element contains expected element",
                DetailsFor.OUT_OF_ORDER, "\"Unresolved reference\"",
            )

            assertThrows<AssertionError> {
                subject.containsNoneIn("Unused variable", "cannot be reassigned")
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                DetailsFor.TESTING_WHETHER, "actual element contains expected element",
                DetailsFor.UNEXPECTED, "[ \"'val' cannot be reassigned\" ]",
            )

            assertThrows<AssertionError> {
                subject.containsExactly("Unresolved reference", "Unused variable")
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
                "Expected exactly all elements from", "[ \"Unresolved reference\", \"Unused variable\" ]",
                DetailsFor.BUT_WAS, "[ \"Unresolved reference 'foo'\", \"'val' cannot be reassigned\" ]",
                DetailsFor.TESTING_WHETHER, "actual element contains expected element",
                DetailsFor.MISSING, "[ \"Unused variable\" ]",
                DetailsFor.EXTRANEOUS, "[ \"'val' cannot be reassigned\" ]",
            )

            assertThrows<AssertionError> {
                subject.containsExactly("cannot be reassigned", "Unresolved reference")
                    .inOrder()
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_ORDERED,
                DetailsFor.TESTING_WHETHER, "actual element contains expected element",
                DetailsFor.OUT_OF_ORDER, "\"Unresolved reference\"",
            )
        }
    }

    @Test
    fun elementsArePairedUpEvenWhenAGreedyPairingWouldFail() {
        // Pairing expected "a" with the first actual element that contains it ("ab") would leave expected "ab" with
        // only actual "a"
        val subject = assertThat(listOf("ab", "a")).comparingElementsUsing(Correspondence.contains)
        subject.containsExactly("a", "ab")
        subject.containsAllIn("a", "ab")

        // Either "ab" or "b" can be paired with actual "ab", and the other is left out
        assertThrows<AssertionError> {
            subject.containsExactly("a", "ab", "b")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            DetailsFor.POSSIBLY_MISSING, "1 of [ \"ab\", \"b\" ]",
        )
    }

    @Test
    fun elementsThatCouldBeLeftOutInsteadOfEachOtherAreReportedTogether() {
        val subject = assertThat(listOf("ab", "a")).comparingElementsUsing(Correspondence.contains)

        // Three expected elements can't pair with two actual ones, and any of them could be the one left out
        assertThrows<AssertionError> {
            subject.containsExactly("a", "a", "ab")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            DetailsFor.POSSIBLY_MISSING, "1 of [ \"a\", \"a\", \"ab\" ]",
        )
        assertThrows<AssertionError> {
            subject.containsExactly("ab", "a", "a")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            DetailsFor.POSSIBLY_MISSING, "1 of [ \"ab\", \"a\", \"a\" ]",
        )
        assertThrows<AssertionError> {
            subject.containsAllIn("a", "a", "ab")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS,
            DetailsFor.POSSIBLY_MISSING, "1 of [ \"a\", \"a\", \"ab\" ]",
        )

        // "zz" matches nothing, so it's certainly missing
        assertThrows<AssertionError> {
            subject.containsExactly("a", "a", "ab", "zz")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            DetailsFor.MISSING, "[ \"zz\" ]",
            DetailsFor.POSSIBLY_MISSING, "1 of [ \"a\", \"a\", \"ab\" ]",
        )

        // More than one can be left out of a group
        assertThrows<AssertionError> {
            assertThat(listOf("abc")).comparingElementsUsing(Correspondence.contains).containsExactly("a", "b", "c")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            DetailsFor.POSSIBLY_MISSING, "2 of [ \"a\", \"b\", \"c\" ]",
        )

        // Groups that don't overlap are listed separately
        assertThrows<AssertionError> {
            assertThat(listOf("ab", "zy"))
                .comparingElementsUsing(Correspondence.contains)
                .containsExactly("a", "ab", "zy", "z")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            DetailsFor.POSSIBLY_MISSING, "1 of [ \"a\", \"ab\" ], 1 of [ \"zy\", \"z\" ]",
        )

        // The same goes for extraneous elements
        assertThrows<AssertionError> {
            assertThat(listOf("a", "ab", "x"))
                .comparingElementsUsing(Correspondence.contains)
                .containsExactly("a")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            DetailsFor.EXTRANEOUS, "[ \"x\" ]",
            DetailsFor.POSSIBLY_EXTRANEOUS, "1 of [ \"a\", \"ab\" ]",
        )
    }

    @Test
    fun failuresAreReportedLikeTheyAreWithoutACorrespondence() {
        // Unexpected elements are listed once each
        assertThrows<AssertionError> {
            assertThat(listOf("'val' cannot be reassigned", "'val' cannot be reassigned", "Unresolved reference 'foo'"))
                .comparingElementsUsing(Correspondence.contains)
                .containsNoneIn("cannot be reassigned")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
            DetailsFor.UNEXPECTED, "[ \"'val' cannot be reassigned\" ]",
        )

        // The extraneous element (27) has two copies, and either could be the extra one, so both are shown
        assertThrows<AssertionError> {
            assertThat((1..30).map { if (it == 16) 27 else it })
                .comparingElementsUsing(Correspondence.within(0.5))
                .containsExactly((1..30).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            DetailsFor.BUT_WAS,
            "[ … (13 more), 14, 15, 27, 17, 18, 19, … (5 more), 25, 26, 27, 28, 29, … (1 more) ]",
            DetailsFor.MISSING, "[ 16 ]",
            DetailsFor.EXTRANEOUS, "[ 27 ]",
        )
    }

    @Test
    fun builtInCorrespondences() {
        assertThat(listOf("Unresolved reference 'foo'", "'val' cannot be reassigned"))
            .comparingElementsUsing(Correspondence.contains(ignoreCase = true))
            .containsExactly("unresolved", "CANNOT BE REASSIGNED")
        assertThrows<AssertionError> {
            assertThat(listOf("Unresolved reference 'foo'"))
                .comparingElementsUsing(Correspondence.contains(ignoreCase = false))
                .contains("unresolved")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS,
            DetailsFor.TESTING_WHETHER, "actual element contains expected element",
        )
        assertThrows<AssertionError> {
            assertThat(listOf("Unresolved reference 'foo'"))
                .comparingElementsUsing(Correspondence.contains(ignoreCase = true))
                .contains("bar")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS,
            DetailsFor.TESTING_WHETHER, "actual element contains (ignoring case) expected element",
        )

        assertThat(listOf("[ERROR] Disk full", "[WARN] Retrying"))
            .comparingElementsUsing(Correspondence.startsWith)
            .containsExactly("[ERROR]", "[WARN]")
        assertThat(listOf("[ERROR] Disk full", "[WARN] Retrying"))
            .comparingElementsUsing(Correspondence.startsWith(ignoreCase = true))
            .containsExactly("[error]", "[warn]")

        assertThat(listOf("photo.png", "notes.txt"))
            .comparingElementsUsing(Correspondence.endsWith)
            .containsExactly(".png", ".txt")
        assertThat(listOf("photo.PNG", "notes.Txt"))
            .comparingElementsUsing(Correspondence.endsWith(ignoreCase = true))
            .containsExactly(".png", ".txt")

        assertThat(listOf("user-12", "admin-3"))
            .comparingElementsUsing(Correspondence.matches)
            .containsExactly(Regex("user-\\d+"), Regex("admin-\\d+"))
        assertThrows<AssertionError> {
            // `matches` checks the whole string
            assertThat(listOf("user-12x"))
                .comparingElementsUsing(Correspondence.matches)
                .contains(Regex("user-\\d+"))
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS,
            DetailsFor.TESTING_WHETHER, "actual element matches expected element",
        )

        assertThat(listOf(0.1 + 0.2, 1.0 / 3))
            .comparingElementsUsing(Correspondence.within(0.001))
            .containsExactly(0.3, 0.333)
        assertThat(listOf(1, 2, 3))
            .comparingElementsUsing(Correspondence.within(0.5))
            .containsExactly(1.2, 2.1, 2.9)
        assertThrows<AssertionError> {
            assertThat(listOf(0.3)).comparingElementsUsing(Correspondence.within(0.001)).contains(0.31)
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS,
            DetailsFor.TESTING_WHETHER, "actual element is within 0.001 of expected element",
        )
        assertThrows<AssertionError> {
            assertThat(listOf(Double.NaN)).comparingElementsUsing(Correspondence.within(0.001)).contains(Double.NaN)
        }
        assertThrows<IllegalArgumentException> {
            Correspondence.within(-1.0)
        }

        data class User(val name: String)
        assertThat(listOf(User("Ann"), User("Bob")))
            .comparingElementsUsing(Correspondence.transforming("is named") { user: User -> user.name })
            .containsExactly("Ann", "Bob")
            .inOrder()
        assertThrows<AssertionError> {
            assertThat(listOf(User("Ann")))
                .comparingElementsUsing(Correspondence.transforming("is named") { user: User -> user.name })
                .contains("Bob")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS,
            DetailsFor.TESTING_WHETHER, "actual element is named expected element",
        )
    }

    @Test
    fun correspondencesCanBeWrittenInline() {
        assertThat(listOf("cat", "horse"))
            .comparingElementsUsing("has length") { word, length: Int -> word.length == length }
            .containsExactly(3, 5)

        assertThrows<AssertionError> {
            assertThat(listOf("cat", "horse"))
                .comparingElementsUsing("has length") { word, length: Int -> word.length == length }
                .containsExactly(3, 4)
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            DetailsFor.TESTING_WHETHER, "actual element has length expected element",
            DetailsFor.MISSING, "[ 4 ]",
            DetailsFor.EXTRANEOUS, "[ \"horse\" ]",
        )
    }

    @Test
    fun mapEntriesCanBeComparedToo() {
        assertThat(mapOf("a" to 1, "b" to 2))
            .comparingElementsUsing(Correspondence.transforming("has key") { entry: Pair<String, Int> -> entry.first })
            .containsExactly("a", "b")
    }

    @Test
    fun theSubjectsMessageAndStrategyStillApply() {
        assertThrows<AssertionError> {
            assertThat(errors).withMessage("Custom message").comparingElementsUsing(Correspondence.contains)
                .contains("Unused variable")
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS,
            DetailsFor.MESSAGE, "Custom message",
        )

        assertThrows<MultipleReportsError> {
            assertAll {
                that(errors).comparingElementsUsing(Correspondence.contains).contains("Unused variable")
                that(errors).comparingElementsUsing(Correspondence.contains).contains("Type mismatch")
            }
        }.let { e ->
            assertThat(e.reports.size).isEqualTo(2)
        }
    }
}
