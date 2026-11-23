package com.varabyte.truthish

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import kotlin.test.Test

class IterableAsserts {
    private val emptyInts
        get() = listOf<Int>()
    private val emptyStrs
        get() = listOf<String>()

    @Test
    fun listChecks() {
        run {
            // Test true statements
            assertThat(listOf("a", "b", "c")).isEqualTo(listOf("a", "b", "c"))
            assertThat(listOf("a", "b", "c")).isNotEqualTo(listOf("a", "b", "d"))

            assertThat(emptyInts).isEmpty()
            assertThat(listOf(1, 2, 3)).isNotEmpty()
            assertThat(listOf(1, 2, 3)).hasSize(3)

            assertThat(listOf("a", "b", "c")).contains("b")
            assertThat(listOf("a", "b", "c")).doesNotContain("d")
            assertThat(emptyStrs).doesNotContain("d")

            assertThat(listOf("a", "b", "c")).hasNoDuplicates()
            assertThat(emptyStrs).hasNoDuplicates()

            assertThat(listOf("a", "b", "c")).containsAnyIn("c", "d", "e")
            assertThat(listOf("a", "b", "c")).containsAnyIn(emptyStrs)
            assertThat(emptyStrs).containsAnyIn(emptyStrs)

            assertThat(listOf("a", "b", "c")).containsAllIn("c", "b")
            assertThat(listOf("a", "b", "c")).containsAllIn("b", "c").inOrder()
            assertThat(listOf("a", "b", "c")).containsAllIn(emptyStrs)

            assertThat(listOf("a", "b", "c")).containsNoneIn("d", "e", "f")
            assertThat(emptyStrs).containsNoneIn("d", "e", "f")

            assertThat(listOf("a", "b", "c")).containsExactly("b", "c", "a")
            assertThat(listOf("a", "b", "c")).containsExactly("a", "b", "c").inOrder()
            assertThat(emptyStrs).containsExactly(emptyStrs)
            assertThat(emptyStrs).containsExactly(emptyStrs).inOrder()
        }

        run {
            // Test false statements
            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "c")).isNotEqualTo(listOf("a", "b", "c"))
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL, "[ \"a\", \"b\", \"c\" ]")

            assertThrows<AssertionError> {
                assertThat(listOf(1, 2, 3)).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThrows<AssertionError> {
                assertThat(emptyInts).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThrows<AssertionError> {
                assertThat(listOf(1, 2, 3)).hasSize(2)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_SIZE, "2", "3", "[ 1, 2, 3 ]")

            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "c")).contains("d")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "c")).doesNotContain("b")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "a")).hasNoDuplicates()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "c")).containsAnyIn("d", "e", "f")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "c")).containsAllIn("c", "b", "d")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "c")).containsAllIn("c", "b", "a").inOrder()
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_ORDERED,
                DetailsFor.IN_ORDER_SO_FAR, "[ \"c\" ]",
                DetailsFor.OUT_OF_ORDER, "\"b\"",
            )

            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "c")).containsNoneIn("c", "d", "e")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS, DetailsFor.UNEXPECTED, "[ \"c\" ]")

            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "c")).containsExactly("b", "b", "c")
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
                DetailsFor.MISSING, "[ \"b\" ]",
                DetailsFor.EXTRANEOUS, "[ \"a\" ]",
            )
            assertThrows<AssertionError> {
                assertThat(listOf("a", "b")).containsExactly("a", "b", "c")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY, DetailsFor.MISSING, "[ \"c\" ]")
            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "c")).containsExactly("c", "b", "a").inOrder()
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_ORDERED,
                DetailsFor.IN_ORDER_SO_FAR, "[ \"c\" ]",
                DetailsFor.OUT_OF_ORDER, "\"b\"",
            )
            assertThrows<AssertionError> {
                assertThat(listOf("a", "a", "b", "c")).containsExactly("a", "b", "c")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY, DetailsFor.EXTRANEOUS, "[ \"a\" ]")
            assertThrows<AssertionError> {
                assertThat(listOf("a", "a", "b", "c")).containsExactly("a", "b", "b", "c")
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
                DetailsFor.MISSING, "[ \"b\" ]",
                DetailsFor.EXTRANEOUS, "[ \"a\" ]",
            )

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThrows<AssertionError> {
                assertThat(listOf("30", "20", "10", "20")).containsExactly("30", "20", "20", "10").inOrder()
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_ORDERED,
                DetailsFor.IN_ORDER_SO_FAR, "[ \"30\", \"20\", \"20\" ]",
                DetailsFor.OUT_OF_ORDER, "\"10\"",
            )

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "c")).containsAllIn("x", "y", "z").inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<AssertionError> {
                assertThat(listOf("a", "b", "c")).containsExactly("c").inOrder()
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY, DetailsFor.EXTRANEOUS, "[ \"a\", \"b\" ]"
            )
        }
    }

    @Test
    fun longListsAreTruncatedAroundTheFirstDifference() {
        // One element replaced
        assertThrows<AssertionError> {
            assertThat((1..30).map { if (it == 16) 99 else it }).isEqualTo((1..30).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.EXPECTED, "[ … (10 more), 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, … (9 more) ]",
            DetailsFor.BUT_WAS, "[ … (10 more), 11, 12, 13, 14, 15, 99, 17, 18, 19, 20, 21, … (9 more) ]",
        )

        // The lists differ only in length
        assertThrows<AssertionError> {
            assertThat((1..20).toList()).isEqualTo((1..30).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.EXPECTED, "[ … (10 more), 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, … (9 more) ]",
            DetailsFor.BUT_WAS, "[ … (10 more), 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 ]",
        )

        // A list short enough to show whole
        assertThrows<AssertionError> {
            assertThat((1..30).toList()).isEqualTo((1..5).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.EXPECTED, "[ 1, 2, 3, 4, 5 ]",
            DetailsFor.BUT_WAS, "[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, … (19 more) ]",
        )
    }

    @Test
    fun setChecks() {
        // Sets are basically just iterables, but we include tests just to make sure using them feels right
        val evensThru10 = (2 .. 10 step 2).toSet()

        run {
            // Test true statements
            assertThat(evensThru10).isNotEmpty()
            assertThat(setOf<Int>()).isEmpty()

            assertThat(evensThru10).contains(8)
            assertThat(evensThru10).doesNotContain(9)

            assertThat(evensThru10).containsAnyIn(1, 2, 3)
            assertThat(evensThru10).containsAllIn(10, 4, 8)
            assertThat(evensThru10).containsAllIn(4, 8, 10).inOrder()
            assertThat(evensThru10).containsNoneIn(1, 11)
            assertThat(evensThru10).containsExactly(2, 4, 6, 8, 10).inOrder()

            assertThat(setOf<Int>()).containsExactly(emptyInts)
        }

        run {
            // Test false statements
            assertThrows<AssertionError> {
                assertThat(evensThru10).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY, DetailsFor.VALUE, "[ 2, 4, 6, 8, 10 ]")
            assertThrows<AssertionError> {
                assertThat(setOf<Int>()).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)

            assertThrows<AssertionError> {
                assertThat(evensThru10).contains(9)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, "9", "[ 2, 4, 6, 8, 10 ]")
            assertThrows<AssertionError> {
                assertThat(evensThru10).doesNotContain(8)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS, "8", "[ 2, 4, 6, 8, 10 ]")

            assertThrows<AssertionError> {
                assertThat(evensThru10).containsAnyIn(1, 3, 5)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, "[ 1, 3, 5 ]", "[ 2, 4, 6, 8, 10 ]")
            assertThrows<AssertionError> {
                assertThat(evensThru10).containsAllIn(1, 4, 8)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, DetailsFor.MISSING, "[ 1 ]")
            assertThrows<AssertionError> {
                assertThat(evensThru10).containsAllIn(8, 10, 4).inOrder()
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_ORDERED,
                "[ 8, 10, 4 ]", "[ 2, 4, 6, 8, 10 ]",
                DetailsFor.IN_ORDER_SO_FAR, "[ 8, 10 ]",
                DetailsFor.OUT_OF_ORDER, "4",
            )
            assertThrows<AssertionError> {
                assertThat(evensThru10).containsNoneIn(2, 5, 7, 8)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS, DetailsFor.UNEXPECTED, "[ 2, 8 ]")
            assertThrows<AssertionError> {
                assertThat(evensThru10).containsExactly(0, 2, 4, 6, 8, 10)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY, DetailsFor.MISSING, "[ 0 ]")
        }
    }

    @Test
    fun longSetsAreTruncatedAtBothEnds() {
        // Sets aren't ordered, so they have no first difference to truncate around
        assertThrows<AssertionError> {
            assertThat((1..30).toSet()).isEqualTo((1..31).toSet())
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.EXPECTED, "{ 1, 2, 3, 4, 5, 6, … (20 more), 27, 28, 29, 30, 31 }",
            DetailsFor.BUT_WAS, "{ 1, 2, 3, 4, 5, 6, … (19 more), 26, 27, 28, 29, 30 }",
        )
    }

    @Test
    fun sequenceChecks() {
        // Sequences are treated as iterables, so just do a few quick sanity checks
        assertThat(sequenceOf("a", "b", "c")).containsExactly("a", "b", "c")
        assertThat(sequenceOf<String>()).isEmpty()
        assertThat(sequenceOf(1, 2, 3)).isNotEmpty()
        assertThat(('a' .. 'z').asSequence()).hasSize(26)
    }
}