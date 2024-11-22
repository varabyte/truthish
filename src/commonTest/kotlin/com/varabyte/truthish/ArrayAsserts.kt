package com.varabyte.truthish

import com.varabyte.truthish.failure.ReportError
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import com.varabyte.truthish.failure.withStrategy
import com.varabyte.truthish.subjects.containsAllIn
import com.varabyte.truthish.subjects.containsAnyIn
import com.varabyte.truthish.subjects.containsExactly
import kotlin.test.Test

class ArrayAsserts {
    private val emptyInts
        get() = IntArray(0)
    private val emptyStrs
        get() = arrayOf<String>()

    @Test
    fun generalArrayChecks() {
        run {
            // Test true statements
            assertThat(arrayOf("a", "b", "c")).isEqualTo(arrayOf("a", "b", "c"))
            assertThat(arrayOf("a", "b", "c")).isNotEqualTo(arrayOf("a", "b", "d"))

            assertThat(emptyInts).isEmpty()
            assertThat(arrayOf(1, 2, 3)).isNotEmpty()
            assertThat(arrayOf(1, 2, 3)).hasSize(3)

            assertThat(arrayOf("a", "b", "c")).contains("b")
            assertThat(arrayOf("a", "b", "c")).doesNotContain("d")
            assertThat(emptyStrs).doesNotContain("d")

            assertThat(arrayOf("a", "b", "c")).hasNoDuplicates()
            assertThat(emptyStrs).hasNoDuplicates()

            assertThat(arrayOf("a", "b", "c")).containsAnyIn("c", "d", "e")
            assertThat(arrayOf("a", "b", "c")).containsAnyIn(emptyStrs)
            assertThat(emptyStrs).containsAnyIn(emptyStrs)

            assertThat(arrayOf("a", "b", "c")).containsAllIn("c", "b")
            assertThat(arrayOf("a", "b", "c")).containsAllIn("b", "c").inOrder()
            assertThat(arrayOf("a", "b", "c")).containsAllIn(emptyStrs)

            assertThat(arrayOf("a", "b", "c")).containsNoneIn("d", "e", "f")
            assertThat(emptyStrs).containsNoneIn("d", "e", "f")

            assertThat(arrayOf("a", "b", "c")).containsExactly("b", "c", "a")
            assertThat(arrayOf("a", "b", "c")).containsExactly("a", "b", "c").inOrder()
            assertThat(emptyStrs).containsExactly(emptyStrs)
            assertThat(emptyStrs).containsExactly(emptyStrs).inOrder()
        }

        run {
            // Test false statements

            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "c")).isNotEqualTo(arrayOf("a", "b", "c"))
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL, "[a, b, c]")


            assertThrows<ReportError> {
                assertThat(arrayOf(1, 2, 3)).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThrows<ReportError> {
                assertThat(emptyInts).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThrows<ReportError> {
                assertThat(arrayOf(1, 2, 3)).hasSize(2)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON)

            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "c")).contains("d")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "c")).doesNotContain("b")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "a")).hasNoDuplicates()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "c")).containsAnyIn("d", "e", "f")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "c")).containsAllIn("c", "b", "d")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "c")).containsAllIn("c", "b", "a").inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "c")).containsNoneIn("c", "d", "e")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "c")).containsExactly("b", "b", "c")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "c")).containsExactly("c", "b", "a").inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThrows<ReportError> {
                assertThat(arrayOf("a", "a", "b", "c")).containsExactly("a", "b", "c")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(arrayOf("a", "a", "b", "c")).containsExactly("a", "b", "b", "c")
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThrows<ReportError> {
                assertThat(arrayOf("30", "20", "10", "20")).containsExactly("30", "20", "20", "10").inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "c")).containsAllIn("x", "y", "z").inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(arrayOf("a", "b", "c")).containsExactly("c").inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
        }
    }

    @Test
    fun intArrayChecks() {
        run {
            // Test true statements
            assertThat(intArrayOf(1, 2, 3)).isEqualTo(intArrayOf(1, 2, 3))
            assertThat(intArrayOf(1, 2, 3)).isNotEqualTo(intArrayOf(1, 2, 4))
            assertThat(intArrayOf(1, 2, 3)).isEqualTo(arrayOf(1, 2, 3))
            assertThat(intArrayOf(1, 2, 3)).isNotEqualTo(arrayOf("a", "b", "c"))

            assertThat(intArrayOf(1, 2, 3)).isNotEmpty()
            assertThat(intArrayOf(1, 2, 3)).hasSize(3)

            assertThat(intArrayOf(1, 2, 3)).contains(2)
            assertThat(intArrayOf(1, 2, 3)).doesNotContain(4)
            assertThat(intArrayOf(1, 2, 3)).hasNoDuplicates()

            assertThat(intArrayOf(1, 2, 3)).containsAnyIn(3, 4, 5)
            assertThat(intArrayOf(1, 2, 3)).containsAnyIn(emptyInts)

            assertThat(intArrayOf(1, 2, 3)).containsAllIn(3, 2)
            assertThat(intArrayOf(1, 2, 3)).containsAllIn(2, 3).inOrder()
            assertThat(intArrayOf(1, 2, 3)).containsAllIn(emptyInts)

            assertThat(intArrayOf(1, 2, 3)).containsNoneIn(4, 5, 6)
            assertThat(emptyInts).containsNoneIn(4, 5, 6)

            assertThat(intArrayOf(1, 2, 3)).containsExactly(2, 3, 1)
            assertThat(intArrayOf(1, 2, 3)).containsExactly(1, 2, 3).inOrder()
            assertThat(emptyInts).containsExactly(emptyInts)
            assertThat(emptyInts).containsExactly(emptyInts).inOrder()
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThrows<ReportError> {
                assertThat(emptyInts).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).hasSize(2)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON)

            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).contains(4)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).doesNotContain(2)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 1)).hasNoDuplicates()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).containsAnyIn(4, 5, 6)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).containsAllIn(3, 2, 4)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).containsAllIn(3, 2, 1).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).containsNoneIn(3, 4, 5)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).containsExactly(2, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).containsExactly(3, 2, 1).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 1, 2, 3)).containsExactly(1, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(arrayOf(1, 1, 2, 3)).containsExactly(1, 2, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThrows<ReportError> {
                assertThat(intArrayOf(30, 20, 10, 20)).containsExactly(30, 20, 20, 10).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).containsAllIn(24, 25, 26).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).containsExactly(3).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
        }
    }
}
