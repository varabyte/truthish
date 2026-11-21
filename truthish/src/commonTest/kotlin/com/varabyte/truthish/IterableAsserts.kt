package com.varabyte.truthish

import com.varabyte.truthish.failure.ReportError
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
            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "c")).isNotEqualTo(listOf("a", "b", "c"))
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL, "[ \"a\", \"b\", \"c\" ]")

            assertThrows<ReportError> {
                assertThat(listOf(1, 2, 3)).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThrows<ReportError> {
                assertThat(emptyInts).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThrows<ReportError> {
                assertThat(listOf(1, 2, 3)).hasSize(2)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON)

            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "c")).contains("d")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "c")).doesNotContain("b")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "a")).hasNoDuplicates()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "c")).containsAnyIn("d", "e", "f")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "c")).containsAllIn("c", "b", "d")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "c")).containsAllIn("c", "b", "a").inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "c")).containsNoneIn("c", "d", "e")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "c")).containsExactly("b", "b", "c")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "c")).containsExactly("c", "b", "a").inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThrows<ReportError> {
                assertThat(listOf("a", "a", "b", "c")).containsExactly("a", "b", "c")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(listOf("a", "a", "b", "c")).containsExactly("a", "b", "b", "c")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThrows<ReportError> {
                assertThat(listOf("30", "20", "10", "20")).containsExactly("30", "20", "20", "10").inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "c")).containsAllIn("x", "y", "z").inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(listOf("a", "b", "c")).containsExactly("c").inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
        }
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
            assertThrows<ReportError> {
                assertThat(evensThru10).isEmpty()
            }
            assertThrows<ReportError> {
                assertThat(setOf<Int>()).isNotEmpty()
            }

            assertThrows<ReportError> {
                assertThat(evensThru10).contains(9)
            }
            assertThrows<ReportError> {
                assertThat(evensThru10).doesNotContain(8)
            }

            assertThrows<ReportError> {
                assertThat(evensThru10).containsAnyIn(1, 3, 5)
            }
            assertThrows<ReportError> {
                assertThat(evensThru10).containsAllIn(1, 4, 8)
            }
            assertThrows<ReportError> {
                assertThat(evensThru10).containsAllIn(8, 10, 4).inOrder()
            }
            assertThrows<ReportError> {
                assertThat(evensThru10).containsNoneIn(2, 5, 7, 8)
            }
            assertThrows<ReportError> {
                assertThat(evensThru10).containsExactly(0, 2, 4, 6, 8, 10)
            }
        }
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