package com.varabyte.truthish

import com.varabyte.truthish.failure.Summaries
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
            val testStrategy = TestStrategy()

            assertThat(arrayOf("a", "b", "c")).withStrategy(testStrategy).isNotEqualTo(arrayOf("a", "b", "c"))
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_EQUAL, "[a, b, c]")

            assertThat(arrayOf(1, 2, 3)).withStrategy(testStrategy).isEmpty()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThat(emptyInts).withStrategy(testStrategy).isNotEmpty()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThat(arrayOf(1, 2, 3)).withStrategy(testStrategy).hasSize(2)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COMPARISON)

            assertThat(arrayOf("a", "b", "c")).withStrategy(testStrategy).contains("d")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(arrayOf("a", "b", "c")).withStrategy(testStrategy).doesNotContain("b")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThat(arrayOf("a", "b", "a")).withStrategy(testStrategy).hasNoDuplicates()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThat(arrayOf("a", "b", "c")).withStrategy(testStrategy).containsAnyIn("d", "e", "f")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThat(arrayOf("a", "b", "c")).withStrategy(testStrategy).containsAllIn("c", "b", "d")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(arrayOf("a", "b", "c")).withStrategy(testStrategy).containsAllIn("c", "b", "a").inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThat(arrayOf("a", "b", "c")).withStrategy(testStrategy).containsNoneIn("c", "d", "e")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThat(arrayOf("a", "b", "c")).withStrategy(testStrategy).containsExactly("b", "b", "c")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(arrayOf("a", "b", "c")).withStrategy(testStrategy).containsExactly("c", "b", "a").inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThat(arrayOf("a", "a", "b", "c")).withStrategy(testStrategy).containsExactly("a", "b", "c")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(arrayOf("a", "a", "b", "c")).withStrategy(testStrategy).containsExactly("a", "b", "b", "c")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThat(arrayOf("30", "20", "10", "20")).withStrategy(testStrategy).containsExactly("30", "20", "20", "10").inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_ORDERED)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThat(arrayOf("a", "b", "c")).withStrategy(testStrategy).containsAllIn("x", "y", "z").inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(arrayOf("a", "b", "c")).withStrategy(testStrategy).containsExactly("c").inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
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
            val testStrategy = TestStrategy()

            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).isEmpty()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThat(emptyInts).withStrategy(testStrategy).isNotEmpty()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).hasSize(2)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COMPARISON)

            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).contains(4)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).doesNotContain(2)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThat(intArrayOf(1, 2, 1)).withStrategy(testStrategy).hasNoDuplicates()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).containsAnyIn(4, 5, 6)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).containsAllIn(3, 2, 4)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).containsAllIn(3, 2, 1).inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).containsNoneIn(3, 4, 5)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).containsExactly(2, 2, 3)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).containsExactly(3, 2, 1).inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThat(intArrayOf(1, 1, 2, 3)).withStrategy(testStrategy).containsExactly(1, 2, 3)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(arrayOf(1, 1, 2, 3)).withStrategy(testStrategy).containsExactly(1, 2, 2, 3)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThat(intArrayOf(30, 20, 10, 20)).withStrategy(testStrategy).containsExactly(30, 20, 20, 10).inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_ORDERED)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).containsAllIn(24, 25, 26).inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(intArrayOf(1, 2, 3)).withStrategy(testStrategy).containsExactly(3).inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
        }
    }
}
