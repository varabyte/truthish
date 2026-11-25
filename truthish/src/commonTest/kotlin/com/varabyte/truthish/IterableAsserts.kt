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
    fun containsAllInCountsDuplicates() {
        assertThat(listOf(1, 2, 1)).containsAllIn(1, 1)
        assertThat(listOf(1, 2, 1)).containsAllIn(1, 1).inOrder()

        assertThrows<AssertionError> {
            assertThat(listOf(1)).containsAllIn(1, 1, 2)
        }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, DetailsFor.MISSING, "[ 1, 2 ]")

        // A missing duplicate isn't blamed on the order
        assertThrows<AssertionError> {
            assertThat(listOf(1)).containsAllIn(1, 1).inOrder()
        }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, DetailsFor.MISSING, "[ 1 ]")

        // But if all the duplicates are there, the order can still be wrong: only one 1 comes after the 2
        assertThrows<AssertionError> {
            assertThat(listOf(1, 2, 1)).containsAllIn(2, 1, 1).inOrder()
        }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED, DetailsFor.OUT_OF_ORDER, "1")
    }

    @Test
    fun elementsThatCauseFailuresAreCutShortAtTheEnd() {
        assertThrows<AssertionError> {
            assertThat((1..5).toList()).containsAllIn((1..20).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS,
            DetailsFor.MISSING, "[ 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, … (4 more) ]",
        )

        assertThrows<AssertionError> {
            assertThat((1..20).toList()).containsExactly(1)
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            DetailsFor.EXTRANEOUS, "[ 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, … (8 more) ]",
        )

        assertThrows<AssertionError> {
            assertThat((1..20).toList()).containsNoneIn((1..20).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
            DetailsFor.UNEXPECTED, "[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, … (9 more) ]",
        )

        assertThrows<AssertionError> {
            assertThat((1..20).toList() + (1..20).toList()).hasNoDuplicates()
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_NO_DUPLICATES,
            DetailsFor.DUPLICATES, "[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, … (9 more) ]",
        )

        assertThrows<AssertionError> {
            assertThat((1..20).toList()).isEqualTo((21..40).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.MISSING, "[ 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, … (9 more) ]",
            DetailsFor.EXTRANEOUS, "[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, … (9 more) ]",
        )
    }

    @Test
    fun valuesThatCauseFailuresAreNeverTruncated() {
        // Single elements that are long lists themselves
        assertThrows<AssertionError> {
            assertThat(listOf(listOf(1))).contains((1..20).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS,
            "[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 ]",
        )

        assertThrows<AssertionError> {
            assertThat(listOf((1..20).toList())).doesNotContain((1..20).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
            "[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 ]",
        )

        assertThrows<AssertionError> {
            assertThat(listOf((1..20).toList(), listOf(1))).containsExactly(listOf(1), (1..20).toList()).inOrder()
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_ORDERED,
            DetailsFor.OUT_OF_ORDER, "[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 ]",
        )
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
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY, DetailsFor.VALUE, "{ 2, 4, 6, 8, 10 }")
            assertThrows<AssertionError> {
                assertThat(setOf<Int>()).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)

            assertThrows<AssertionError> {
                assertThat(evensThru10).contains(9)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, "9", "{ 2, 4, 6, 8, 10 }")
            assertThrows<AssertionError> {
                assertThat(evensThru10).doesNotContain(8)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS, "8", "{ 2, 4, 6, 8, 10 }")

            assertThrows<AssertionError> {
                assertThat(evensThru10).containsAnyIn(1, 3, 5)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, "[ 1, 3, 5 ]", "{ 2, 4, 6, 8, 10 }")
            assertThrows<AssertionError> {
                assertThat(evensThru10).containsAllIn(1, 4, 8)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, DetailsFor.MISSING, "[ 1 ]")
            assertThrows<AssertionError> {
                assertThat(evensThru10).containsAllIn(8, 10, 4).inOrder()
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_ORDERED,
                "[ 8, 10, 4 ]", "{ 2, 4, 6, 8, 10 }",
                DetailsFor.OUT_OF_ORDER, "4",
            )
            assertThrows<AssertionError> {
                assertThat(evensThru10).containsNoneIn(2, 5, 7, 8)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS, DetailsFor.UNEXPECTED, "[ 2, 8 ]")
            assertThrows<AssertionError> {
                assertThat(evensThru10).containsExactly(0, 2, 4, 6, 8, 10)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY, DetailsFor.MISSING, "[ 0 ]")
            assertThrows<AssertionError> {
                assertThat(evensThru10).containsExactly(setOf(2, 4, 6))
            }.assertSubstrings(
                Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
                "{ 2, 4, 6 }", "{ 2, 4, 6, 8, 10 }",
                DetailsFor.EXTRANEOUS, "[ 8, 10 ]",
            )
            assertThrows<AssertionError> {
                assertThat(evensThru10).containsExactly(setOf(10, 8, 6, 4, 2)).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED, "{ 10, 8, 6, 4, 2 }", "{ 2, 4, 6, 8, 10 }")
        }
    }

    @Test
    fun longListsAreTruncatedAroundTheElementThatCausedTheFailure() {
        assertThrows<AssertionError> {
            assertThat((1..30).toList()).doesNotContain(16)
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
            DetailsFor.BUT_WAS, "[ … (10 more), 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, … (9 more) ]",
        )

        // Both copies of the first duplicate (3), even though there are more (7)
        assertThrows<AssertionError> {
            assertThat((1..30).map { if (it == 16) 3 else if (it == 25) 7 else it }).hasNoDuplicates()
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_NO_DUPLICATES,
            DetailsFor.VALUE, "[ 1, 2, 3, 4, 5, 6, … (7 more), 14, 15, 3, 17, 18, … (12 more) ]",
            DetailsFor.DUPLICATES, "[ 3, 7 ]",
        )

        // Both lists are centered on their copy of the first shared element (16)
        assertThrows<AssertionError> {
            assertThat((1..30).toList()).containsNoneIn((40..69).map { if (it == 55) 16 else it })
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
            "[ … (10 more), 50, 51, 52, 53, 54, 16, 56, 57, 58, 59, 60, … (9 more) ]",
            DetailsFor.BUT_WAS, "[ … (10 more), 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, … (9 more) ]",
            DetailsFor.UNEXPECTED, "[ 16 ]",
        )
    }

    @Test
    fun longListsAreTruncatedAroundWhatIsOutOfPlace() {
        // Expected is centered on its first missing element (5), and But was on its first extraneous one (99)
        assertThrows<AssertionError> {
            assertThat((1..30).filter { it != 5 } + 99).containsExactly((1..30).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            "[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, … (19 more) ]",
            DetailsFor.BUT_WAS, "[ … (19 more), 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 99 ]",
            DetailsFor.MISSING, "[ 5 ]",
            DetailsFor.EXTRANEOUS, "[ 99 ]",
        )

        // Expected is centered on its first missing element (20)
        assertThrows<AssertionError> {
            assertThat((1..30).filter { it != 20 }).containsAllIn((1..30).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS,
            "[ … (14 more), 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, … (5 more) ]",
            DetailsFor.MISSING, "[ 20 ]",
        )

        // The extraneous element (27) has two copies, and either could be the extra one, so both are shown
        assertThrows<AssertionError> {
            assertThat((1..30).map { if (it == 16) 27 else it }).containsExactly((1..30).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
            DetailsFor.BUT_WAS,
            "[ … (13 more), 14, 15, 27, 17, 18, 19, … (5 more), 25, 26, 27, 28, 29, … (1 more) ]",
            DetailsFor.MISSING, "[ 16 ]",
            DetailsFor.EXTRANEOUS, "[ 27 ]",
        )

        // Expected is centered on the out-of-order element (11). But was shows where it is in the actual list, and
        // where the element expected before it (20) was found.
        assertThrows<AssertionError> {
            assertThat((1..30).toList())
                .containsExactly((1..30).map { if (it == 10) 20 else if (it == 20) 10 else it })
                .inOrder()
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_ORDERED,
            "[ … (5 more), 6, 7, 8, 9, 20, 11, 12, 13, 14, 15, 16, … (14 more) ]",
            DetailsFor.BUT_WAS,
            "[ … (7 more), 8, 9, 10, 11, 12, 13, … (4 more), 18, 19, 20, 21, 22, … (8 more) ]",
            DetailsFor.OUT_OF_ORDER, "11",
        )
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
    fun unequalCollectionsListWhatEachSideLacks() {
        assertThrows<AssertionError> {
            assertThat(listOf(1, 2, 3, 4)).isEqualTo(listOf(1, 2, 5, 4))
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.MISSING, "[ 5 ]",
            DetailsFor.EXTRANEOUS, "[ 3 ]",
        )

        // Differences hidden by truncation are still listed
        assertThrows<AssertionError> {
            assertThat((1..30).map { if (it == 3) 98 else if (it == 28) 99 else it }).isEqualTo((1..30).toList())
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.EXPECTED, "[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, … (19 more) ]",
            DetailsFor.BUT_WAS, "[ 1, 2, 98, 4, 5, 6, 7, 8, 9, 10, 11, … (19 more) ]",
            DetailsFor.MISSING, "[ 3, 28 ]",
            DetailsFor.EXTRANEOUS, "[ 98, 99 ]",
        )

        assertThrows<AssertionError> {
            assertThat(listOf("a", "b", "b")).isEqualTo(listOf("a", "a", "b"))
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.MISSING, "[ \"a\" ]",
            DetailsFor.EXTRANEOUS, "[ \"b\" ]",
        )

        assertThrows<AssertionError> {
            assertThat((1..30).toList()).isEqualTo((1..30).map { if (it == 5) 25 else if (it == 25) 5 else it })
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.NOTE, "Same elements, in a different order",
        )

        assertThrows<AssertionError> {
            assertThat(setOf(1, 2, 3)).isEqualTo(setOf(1, 2, 4))
        }.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.MISSING, "[ 4 ]",
            DetailsFor.EXTRANEOUS, "[ 3 ]",
        )

        // A set never equals a list, so there's nothing to list
        val e = assertThrows<AssertionError> {
            assertThat(setOf(1, 2, 3)).isEqualTo(listOf(1, 2, 3))
        }
        e.assertSubstrings(
            Summaries.EXPECTED_EQUAL,
            DetailsFor.EXPECTED, "[ 1, 2, 3 ]",
            DetailsFor.BUT_WAS, "{ 1, 2, 3 }",
        )
        assertThat(e.message!!).doesNotContain(DetailsFor.MISSING)
        assertThat(e.message!!).doesNotContain(DetailsFor.EXTRANEOUS)
        assertThat(e.message!!).doesNotContain(DetailsFor.NOTE)
    }

    @Test
    fun sequenceChecks() {
        // Sequences are treated as iterables, so just do a few quick sanity checks
        assertThat(sequenceOf("a", "b", "c")).containsExactly("a", "b", "c")
        assertThat(sequenceOf<String>()).isEmpty()
        assertThat(sequenceOf(1, 2, 3)).isNotEmpty()
        assertThat(('a' .. 'z').asSequence()).hasSize(26)
        // Some sequences can only be iterated once
        assertThat(sequenceOf(1, 2, 3).constrainOnce()).containsExactly(1, 2, 3).inOrder()
        assertThrows<AssertionError> {
            assertThat(sequenceOf(1, 2).constrainOnce()).isEqualTo(listOf(1, 3))
        }.assertSubstrings(Summaries.EXPECTED_EQUAL, DetailsFor.BUT_WAS, "[ 1, 2 ]")
    }
}