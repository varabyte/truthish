package bitspittle.truthish

import bitspittle.truthish.failure.Summaries
import bitspittle.truthish.failure.withStrategy
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
            val testStrategy = TestStrategy()

            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).isNotEqualTo(listOf("a", "b", "c"))
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_EQUAL, "[ \"a\", \"b\", \"c\" ]")

            assertThat(listOf(1, 2, 3)).withStrategy(testStrategy).isEmpty()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThat(emptyInts).withStrategy(testStrategy).isNotEmpty()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThat(listOf(1, 2, 3)).withStrategy(testStrategy).hasSize(2)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COMPARISON)

            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).contains("d")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).doesNotContain("b")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThat(listOf("a", "b", "a")).withStrategy(testStrategy).hasNoDuplicates()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).containsAnyIn("d", "e", "f")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).containsAllIn("c", "b", "d")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).containsAllIn("c", "b", "a").inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).containsNoneIn("c", "d", "e")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).containsExactly("b", "b", "c")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).containsExactly("c", "b", "a").inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThat(listOf("a", "a", "b", "c")).withStrategy(testStrategy).containsExactly("a", "b", "c")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(listOf("a", "a", "b", "c")).withStrategy(testStrategy).containsExactly("a", "b", "b", "c")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).containsAllIn("x", "y", "z").inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThat(listOf("a", "b", "c")).withStrategy(testStrategy).containsExactly("c").inOrder()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_COLLECTION_CONTAINS)
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
            val testStrategy = TestStrategy()

            assertThat(evensThru10).withStrategy(testStrategy).isEmpty()
            testStrategy.verifyFailureAndClear()
            assertThat(setOf<Int>()).withStrategy(testStrategy).isNotEmpty()
            testStrategy.verifyFailureAndClear()

            assertThat(evensThru10).withStrategy(testStrategy).contains(9)
            testStrategy.verifyFailureAndClear()
            assertThat(evensThru10).withStrategy(testStrategy).doesNotContain(8)
            testStrategy.verifyFailureAndClear()

            assertThat(evensThru10).withStrategy(testStrategy).containsAnyIn(1, 3, 5)
            testStrategy.verifyFailureAndClear()
            assertThat(evensThru10).withStrategy(testStrategy).containsAllIn(1, 4, 8)
            testStrategy.verifyFailureAndClear()
            assertThat(evensThru10).withStrategy(testStrategy).containsAllIn(8, 10, 4).inOrder()
            testStrategy.verifyFailureAndClear()
            assertThat(evensThru10).withStrategy(testStrategy).containsNoneIn(2, 5, 7, 8)
            testStrategy.verifyFailureAndClear()
            assertThat(evensThru10).withStrategy(testStrategy).containsExactly(0, 2, 4, 6, 8, 10)
            testStrategy.verifyFailureAndClear()
        }
    }

    @Test
    fun mapChecks() {
        val asciiMap = ('a'..'z').associateWith { it.code }

        run {
            assertThat(asciiMap.keys).contains('g')
            assertThat(asciiMap.keys).doesNotContain('G')
            assertThat(asciiMap.values).contains(107)
            assertThat(asciiMap.values).doesNotContain(9999)
            assertThat(asciiMap).contains('e' to 101)
            assertThat(asciiMap).doesNotContain('e' to 102)

            assertThat(asciiMap).containsAllIn('a' to 97, 'z' to 122).inOrder()
        }

        run {
            // Test false statements
            val testStrategy = TestStrategy()

            assertThat(asciiMap.keys).withStrategy(testStrategy).doesNotContain('g')
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap.keys).withStrategy(testStrategy).contains('G')
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap.values).withStrategy(testStrategy).doesNotContain(107)
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap.values).withStrategy(testStrategy).contains(9999)
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap).withStrategy(testStrategy).doesNotContain('e' to 101)
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap).withStrategy(testStrategy).contains('e' to 102)
            testStrategy.verifyFailureAndClear()

            assertThat(asciiMap).withStrategy(testStrategy).containsAllIn('a' to 122, 'z' to 122)
            testStrategy.verifyFailureAndClear()
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