package com.varabyte.truthish

import com.varabyte.truthish.failure.ReportError
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import com.varabyte.truthish.subjects.containsAllIn
import com.varabyte.truthish.subjects.containsAnyIn
import com.varabyte.truthish.subjects.containsExactly
import kotlin.test.Test

class ArrayAsserts {
    private val emptyBools
        get() = BooleanArray(0)
    private val emptyBytes
        get() = ByteArray(0)
    private val emptyChars
        get() = CharArray(0)
    private val emptyShorts
        get() = ShortArray(0)
    private val emptyInts
        get() = IntArray(0)
    private val emptyLongs
        get() = LongArray(0)
    private val emptyFloats
        get() = FloatArray(0)
    private val emptyDoubles
        get() = DoubleArray(0)

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
                assertThat(arrayOf("a", "b", "c")).isEqualTo(arrayOf("a", "b", "d"))
            }.assertSubstrings(Summaries.EXPECTED_EQUAL, "[a, b, d]")
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
    fun boolArrayChecks() {
        run {
            // Test true statements
            assertThat(booleanArrayOf(true, false, true)).isEqualTo(
                booleanArrayOf(true, false, true)
            )
            assertThat(booleanArrayOf(true, false, true)).isNotEqualTo(booleanArrayOf(true, false, false))
            assertThat(booleanArrayOf(true, false, true)).isEqualTo(arrayOf(true, false, true))
            assertThat(booleanArrayOf(true, false, true)).isNotEqualTo(arrayOf(1, 2, 3))

            assertThat(booleanArrayOf(true, false, true)).isNotEmpty()
            assertThat(booleanArrayOf(true, false, true)).hasSize(3)

            assertThat(booleanArrayOf(true, true)).contains(true)
            assertThat(booleanArrayOf(true, true)).doesNotContain(false)
            assertThat(booleanArrayOf(true, false)).hasNoDuplicates()

            assertThat(booleanArrayOf(true)).containsAnyIn(true, false)
            assertThat(booleanArrayOf(true)).containsAnyIn(emptyBools)

            assertThat(booleanArrayOf(true, false)).containsAllIn(true, false)
            assertThat(booleanArrayOf(true, false)).containsAllIn(true, false).inOrder()
            assertThat(booleanArrayOf(true, false)).containsAllIn(emptyBools)

            assertThat(booleanArrayOf(true, true)).containsNoneIn(false)
            assertThat(booleanArrayOf(true, true)).containsNoneIn(booleanArrayOf(false))
            assertThat(emptyBools).containsNoneIn(true, false)

            assertThat(booleanArrayOf(true, false, true)).containsExactly(true, true, false)
            assertThat(booleanArrayOf(true, false, true)).containsExactly(true, false, true).inOrder()
            assertThat(emptyBools).containsExactly(emptyBools)
            assertThat(emptyBools).containsExactly(emptyBools).inOrder()
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, false, true)).isEqualTo(booleanArrayOf(true, true, false))
            }.assertSubstrings(Summaries.EXPECTED_EQUAL)
            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, false, true)).isNotEqualTo(booleanArrayOf(true, false, true))
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL)

            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, false, true)).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThrows<ReportError> {
                assertThat(emptyInts).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, false, true)).hasSize(2)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON)

            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, true)).contains(false)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, false, true)).doesNotContain(false)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, false, true)).hasNoDuplicates()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, true)).containsAnyIn(false)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, true)).containsAllIn(true, false)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, false)).containsAllIn(false, true).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true)).containsNoneIn(true, false)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, false, true)).containsExactly(true, true, true)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, false, true)).containsExactly(true, true, false).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThrows<ReportError> {
                assertThat(booleanArrayOf(true, true, false, true)).containsExactly(true, false, true)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
        }
    }

    @Test
    fun byteArrayChecks() {
        run {
            // Test true statements
            assertThat(byteArrayOf(1, 2, 3)).isEqualTo(byteArrayOf(1, 2, 3))
            assertThat(byteArrayOf(1, 2, 3)).isNotEqualTo(byteArrayOf(1, 2, 4))
            assertThat(byteArrayOf(1, 2, 3)).isEqualTo(arrayOf(1, 2, 3))
            assertThat(byteArrayOf(1, 2, 3)).isNotEqualTo(arrayOf("a", "b", "c"))

            assertThat(byteArrayOf(1, 2, 3)).isNotEmpty()
            assertThat(byteArrayOf(1, 2, 3)).hasSize(3)

            assertThat(byteArrayOf(1, 2, 3)).contains(2)
            assertThat(byteArrayOf(1, 2, 3)).doesNotContain(4)
            assertThat(byteArrayOf(1, 2, 3)).hasNoDuplicates()

            assertThat(byteArrayOf(1, 2, 3)).containsAnyIn(3, 4, 5)
            assertThat(byteArrayOf(1, 2, 3)).containsAnyIn(emptyBytes)

            assertThat(byteArrayOf(1, 2, 3)).containsAllIn(3, 2)
            assertThat(byteArrayOf(1, 2, 3)).containsAllIn(2, 3).inOrder()
            assertThat(byteArrayOf(1, 2, 3)).containsAllIn(emptyBytes)

            assertThat(byteArrayOf(1, 2, 3)).containsNoneIn(4, 5, 6)
            assertThat(byteArrayOf(1, 2, 3)).containsNoneIn(byteArrayOf(4, 5, 6))
            assertThat(emptyBytes).containsNoneIn(4, 5, 6)

            assertThat(byteArrayOf(1, 2, 3)).containsExactly(2, 3, 1)
            assertThat(byteArrayOf(1, 2, 3)).containsExactly(1, 2, 3).inOrder()
            assertThat(emptyBytes).containsExactly(emptyBytes)
            assertThat(emptyBytes).containsExactly(emptyBytes).inOrder()
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).isEqualTo(byteArrayOf(1, 2, 4))
            }.assertSubstrings(Summaries.EXPECTED_EQUAL)
            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).isNotEqualTo(byteArrayOf(1, 2, 3))
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL)


            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThrows<ReportError> {
                assertThat(emptyBytes).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).hasSize(2)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON)

            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).contains(4)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).doesNotContain(2)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 1)).hasNoDuplicates()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).containsAnyIn(4, 5, 6)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).containsAllIn(3, 2, 4)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).containsAllIn(3, 2, 1).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).containsNoneIn(3, 4, 5)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).containsExactly(2, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).containsExactly(3, 2, 1).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 1, 2, 3)).containsExactly(1, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 1, 2, 3)).containsExactly(1, 2, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThrows<ReportError> {
                assertThat(byteArrayOf(30, 20, 10, 20)).containsExactly(30, 20, 20, 10).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).containsAllIn(24, 25, 26).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(byteArrayOf(1, 2, 3)).containsExactly(3).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
        }
    }

    @Test
    fun charArrayChecks() {
        run {
            // Test true statements
            assertThat(charArrayOf('a', 'b', 'c')).isEqualTo(charArrayOf('a', 'b', 'c'))
            assertThat(charArrayOf('a', 'b', 'c')).isNotEqualTo(charArrayOf('a', 'b', 'd'))
            assertThat(charArrayOf('a', 'b', 'c')).isEqualTo(arrayOf('a', 'b', 'c'))
            assertThat(charArrayOf('a', 'b', 'c')).isNotEqualTo(arrayOf("a", "b", "c"))

            assertThat(charArrayOf('a', 'b', 'c')).isNotEmpty()
            assertThat(charArrayOf('a', 'b', 'c')).hasSize(3)

            assertThat(charArrayOf('a', 'b', 'c')).contains('b')
            assertThat(charArrayOf('a', 'b', 'c')).doesNotContain('d')
            assertThat(charArrayOf('a', 'b', 'c')).hasNoDuplicates()

            assertThat(charArrayOf('a', 'b', 'c')).containsAnyIn('c', 'd', 'e')
            assertThat(charArrayOf('a', 'b', 'c')).containsAnyIn(emptyChars)

            assertThat(charArrayOf('a', 'b', 'c')).containsAllIn('c', 'b')
            assertThat(charArrayOf('a', 'b', 'c')).containsAllIn('b', 'c').inOrder()
            assertThat(charArrayOf('a', 'b', 'c')).containsAllIn(emptyChars)

            assertThat(charArrayOf('a', 'b', 'c')).containsNoneIn('d', 'e', 'f')
            assertThat(charArrayOf('a', 'b', 'c')).containsNoneIn(charArrayOf('d', 'e', 'f'))
            assertThat(emptyChars).containsNoneIn('d', 'e', 'f')

            assertThat(charArrayOf('a', 'b', 'c')).containsExactly('b', 'c', 'a')
            assertThat(charArrayOf('a', 'b', 'c')).containsExactly('a', 'b', 'c').inOrder()
            assertThat(emptyChars).containsExactly(emptyChars)
            assertThat(emptyChars).containsExactly(emptyChars).inOrder()
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).isEqualTo(charArrayOf('a', 'b', 'd'))
            }.assertSubstrings(Summaries.EXPECTED_EQUAL)
            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).isNotEqualTo(charArrayOf('a', 'b', 'c'))
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL)

            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThrows<ReportError> {
                assertThat(emptyChars).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).hasSize(2)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON)

            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).contains('d')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).doesNotContain('b')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'a')).hasNoDuplicates()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).containsAnyIn('d', 'e', 'f')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).containsAllIn('c', 'b', 'd')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).containsAllIn('c', 'b', 'a').inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).containsNoneIn('c', 'd', 'e')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).containsExactly('b', 'b', 'c')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).containsExactly('c', 'b', 'a').inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'a', 'b', 'c')).containsExactly('a', 'b', 'c')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'a', 'b', 'c')).containsExactly('a', 'b', 'b', 'c')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThrows<ReportError> {
                assertThat(charArrayOf('c', 'b', 'a', 'b')).containsExactly('c', 'b', 'b', 'a').inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).containsAllIn('x', 'y', 'z').inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(charArrayOf('a', 'b', 'c')).containsExactly('a').inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
        }
    }

    @Test
    fun shortArrayChecks() {
        run {
            // Test true statements
            assertThat(shortArrayOf(1, 2, 3)).isEqualTo(shortArrayOf(1, 2, 3))
            assertThat(shortArrayOf(1, 2, 3)).isNotEqualTo(shortArrayOf(1, 2, 4))
            assertThat(shortArrayOf(1, 2, 3)).isEqualTo(arrayOf(1, 2, 3))
            assertThat(shortArrayOf(1, 2, 3)).isNotEqualTo(arrayOf("a", "b", "c"))

            assertThat(shortArrayOf(1, 2, 3)).isNotEmpty()
            assertThat(shortArrayOf(1, 2, 3)).hasSize(3)

            assertThat(shortArrayOf(1, 2, 3)).contains(2)
            assertThat(shortArrayOf(1, 2, 3)).doesNotContain(4)
            assertThat(shortArrayOf(1, 2, 3)).hasNoDuplicates()

            assertThat(shortArrayOf(1, 2, 3)).containsAnyIn(3, 4, 5)
            assertThat(shortArrayOf(1, 2, 3)).containsAnyIn(emptyShorts)

            assertThat(shortArrayOf(1, 2, 3)).containsAllIn(3, 2)
            assertThat(shortArrayOf(1, 2, 3)).containsAllIn(2, 3).inOrder()
            assertThat(shortArrayOf(1, 2, 3)).containsAllIn(emptyShorts)

            assertThat(shortArrayOf(1, 2, 3)).containsNoneIn(4, 5, 6)
            assertThat(shortArrayOf(1, 2, 3)).containsNoneIn(shortArrayOf(4, 5, 6))
            assertThat(emptyShorts).containsNoneIn(4, 5, 6)

            assertThat(shortArrayOf(1, 2, 3)).containsExactly(2, 3, 1)
            assertThat(shortArrayOf(1, 2, 3)).containsExactly(1, 2, 3).inOrder()
            assertThat(emptyShorts).containsExactly(emptyShorts)
            assertThat(emptyShorts).containsExactly(emptyShorts).inOrder()
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).isEqualTo(shortArrayOf(1, 2, 4))
            }.assertSubstrings(Summaries.EXPECTED_EQUAL)
            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).isNotEqualTo(shortArrayOf(1, 2, 3))
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL)

            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThrows<ReportError> {
                assertThat(emptyShorts).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).hasSize(2)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON)

            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).contains(4)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).doesNotContain(2)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 1)).hasNoDuplicates()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).containsAnyIn(4, 5, 6)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).containsAllIn(3, 2, 4)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).containsAllIn(3, 2, 1).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).containsNoneIn(3, 4, 5)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).containsExactly(2, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).containsExactly(3, 2, 1).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 1, 2, 3)).containsExactly(1, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 1, 2, 3)).containsExactly(1, 2, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThrows<ReportError> {
                assertThat(shortArrayOf(30, 20, 10, 20)).containsExactly(30, 20, 20, 10).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).containsAllIn(24, 25, 26).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(shortArrayOf(1, 2, 3)).containsExactly(3).inOrder()
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
            assertThat(intArrayOf(1, 2, 3)).containsNoneIn(intArrayOf(4, 5, 6))
            assertThat(emptyInts).containsNoneIn(4, 5, 6)

            assertThat(intArrayOf(1, 2, 3)).containsExactly(2, 3, 1)
            assertThat(intArrayOf(1, 2, 3)).containsExactly(1, 2, 3).inOrder()
            assertThat(emptyInts).containsExactly(emptyInts)
            assertThat(emptyInts).containsExactly(emptyInts).inOrder()
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).isEqualTo(intArrayOf(1, 2, 4))
            }.assertSubstrings(Summaries.EXPECTED_EQUAL)
            assertThrows<ReportError> {
                assertThat(intArrayOf(1, 2, 3)).isNotEqualTo(intArrayOf(1, 2, 3))
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL)

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
                assertThat(intArrayOf(1, 1, 2, 3)).containsExactly(1, 2, 2, 3)
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

    @Test
    fun longArrayChecks() {
        run {
            // Test true statements
            assertThat(longArrayOf(1, 2, 3)).isEqualTo(longArrayOf(1, 2, 3))
            assertThat(longArrayOf(1, 2, 3)).isNotEqualTo(longArrayOf(1, 2, 4))
            assertThat(longArrayOf(1, 2, 3)).isEqualTo(arrayOf(1, 2, 3))
            assertThat(longArrayOf(1, 2, 3)).isNotEqualTo(arrayOf("a", "b", "c"))

            assertThat(longArrayOf(1, 2, 3)).isNotEmpty()
            assertThat(longArrayOf(1, 2, 3)).hasSize(3)

            assertThat(longArrayOf(1, 2, 3)).contains(2)
            assertThat(longArrayOf(1, 2, 3)).doesNotContain(4)
            assertThat(longArrayOf(1, 2, 3)).hasNoDuplicates()

            assertThat(longArrayOf(1, 2, 3)).containsAnyIn(3, 4, 5)
            assertThat(longArrayOf(1, 2, 3)).containsAnyIn(emptyLongs)

            assertThat(longArrayOf(1, 2, 3)).containsAllIn(3, 2)
            assertThat(longArrayOf(1, 2, 3)).containsAllIn(2, 3).inOrder()
            assertThat(longArrayOf(1, 2, 3)).containsAllIn(emptyLongs)

            assertThat(longArrayOf(1, 2, 3)).containsNoneIn(4, 5, 6)
            assertThat(longArrayOf(1, 2, 3)).containsNoneIn(longArrayOf(4, 5, 6))
            assertThat(emptyLongs).containsNoneIn(4, 5, 6)

            assertThat(longArrayOf(1, 2, 3)).containsExactly(2, 3, 1)
            assertThat(longArrayOf(1, 2, 3)).containsExactly(1, 2, 3).inOrder()
            assertThat(emptyLongs).containsExactly(emptyLongs)
            assertThat(emptyLongs).containsExactly(emptyLongs).inOrder()
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).isEqualTo(longArrayOf(1, 2, 4))
            }.assertSubstrings(Summaries.EXPECTED_EQUAL)
            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).isNotEqualTo(longArrayOf(1, 2, 3))
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL)

            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThrows<ReportError> {
                assertThat(emptyLongs).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).hasSize(2)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON)

            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).contains(4)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).doesNotContain(2)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 1)).hasNoDuplicates()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).containsAnyIn(4, 5, 6)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).containsAllIn(3, 2, 4)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).containsAllIn(3, 2, 1).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).containsNoneIn(3, 4, 5)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).containsExactly(2, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).containsExactly(3, 2, 1).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 1, 2, 3)).containsExactly(1, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 1, 2, 3)).containsExactly(1, 2, 2, 3)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThrows<ReportError> {
                assertThat(longArrayOf(30, 20, 10, 20)).containsExactly(30, 20, 20, 10).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).containsAllIn(24, 25, 26).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(longArrayOf(1, 2, 3)).containsExactly(3).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
        }
    }

    @Test
    fun floatArrayChecks() {
        run {
            // Test true statements
            assertThat(floatArrayOf(1f, 2f, 3f)).isEqualTo(floatArrayOf(1f, 2f, 3f))
            assertThat(floatArrayOf(1f, 2f, 3f)).isNotEqualTo(floatArrayOf(1f, 2f, 4f))
            assertThat(floatArrayOf(1f, 2f, 3f)).isEqualTo(arrayOf(1f, 2f, 3f))
            assertThat(floatArrayOf(1f, 2f, 3f)).isNotEqualTo(arrayOf("a", "b", "c"))

            assertThat(floatArrayOf(1f, 2f, 3f)).isNotEmpty()
            assertThat(floatArrayOf(1f, 2f, 3f)).hasSize(3)

            assertThat(floatArrayOf(1f, 2f, 3f)).contains(2f)
            assertThat(floatArrayOf(1f, 2f, 3f)).doesNotContain(4f)
            assertThat(floatArrayOf(1f, 2f, 3f)).hasNoDuplicates()

            assertThat(floatArrayOf(1f, 2f, 3f)).containsAnyIn(3f, 4f, 5f)
            assertThat(floatArrayOf(1f, 2f, 3f)).containsAnyIn(emptyFloats)

            assertThat(floatArrayOf(1f, 2f, 3f)).containsAllIn(3f, 2f)
            assertThat(floatArrayOf(1f, 2f, 3f)).containsAllIn(2f, 3f).inOrder()
            assertThat(floatArrayOf(1f, 2f, 3f)).containsAllIn(emptyFloats)

            assertThat(floatArrayOf(1f, 2f, 3f)).containsNoneIn(4f, 5f, 6f)
            assertThat(floatArrayOf(1f, 2f, 3f)).containsNoneIn(floatArrayOf(4f, 5f, 6f))
            assertThat(emptyFloats).containsNoneIn(4f, 5f, 6f)

            assertThat(floatArrayOf(1f, 2f, 3f)).containsExactly(2f, 3f, 1f)
            assertThat(floatArrayOf(1f, 2f, 3f)).containsExactly(1f, 2f, 3f).inOrder()
            assertThat(emptyFloats).containsExactly(emptyFloats)
            assertThat(emptyFloats).containsExactly(emptyFloats).inOrder()
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).isEqualTo(floatArrayOf(1f, 2f, 4f))
            }.assertSubstrings(Summaries.EXPECTED_EQUAL)
            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).isNotEqualTo(floatArrayOf(1f, 2f, 3f))
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL)

            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThrows<ReportError> {
                assertThat(emptyFloats).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).hasSize(2)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON)

            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).contains(4f)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).doesNotContain(2f)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 1f)).hasNoDuplicates()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).containsAnyIn(4f, 5f, 6f)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).containsAllIn(3f, 2f, 4f)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).containsAllIn(3f, 2f, 1f).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).containsNoneIn(3f, 4f, 5f)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).containsExactly(2f, 2f, 3f)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).containsExactly(3f, 2f, 1f).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 1f, 2f, 3f)).containsExactly(1f, 2f, 3f)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 1f, 2f, 3f)).containsExactly(1f, 2f, 2f, 3f)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThrows<ReportError> {
                assertThat(floatArrayOf(3f, 2f, 1f, 2f)).containsExactly(3f, 2f, 2f, 1f).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).containsAllIn(4f, 5f, 6f).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(floatArrayOf(1f, 2f, 3f)).containsExactly(3f).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
        }
    }

    @Test
    fun doubleArrayChecks() {
        run {
            // Test true statements
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).isEqualTo(doubleArrayOf(1.0, 2.0, 3.0))
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).isNotEqualTo(doubleArrayOf(1.0, 2.0, 4.0))
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).isEqualTo(arrayOf(1.0, 2.0, 3.0))
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).isNotEqualTo(arrayOf("a", "b", "c"))

            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).isNotEmpty()
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).hasSize(3)

            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).contains(2.0)
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).doesNotContain(4.0)
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).hasNoDuplicates()

            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsAnyIn(3.0, 4.0, 5.0)
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsAnyIn(emptyDoubles)

            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsAllIn(3.0, 2.0)
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsAllIn(2.0, 3.0).inOrder()
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsAllIn(emptyDoubles)

            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsNoneIn(4.0, 5.0, 6.0)
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsNoneIn(doubleArrayOf(4.0, 5.0, 6.0))
            assertThat(emptyDoubles).containsNoneIn(4.0, 5.0, 6.0)

            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsExactly(2.0, 3.0, 1.0)
            assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsExactly(1.0, 2.0, 3.0).inOrder()
            assertThat(emptyDoubles).containsExactly(emptyDoubles)
            assertThat(emptyDoubles).containsExactly(emptyDoubles).inOrder()
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).isEqualTo(doubleArrayOf(1.0, 2.0, 4.0))
            }.assertSubstrings(Summaries.EXPECTED_EQUAL)
            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).isNotEqualTo(doubleArrayOf(1.0, 2.0, 3.0))
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL)

            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).isEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_EMPTY)
            assertThrows<ReportError> {
                assertThat(emptyDoubles).isNotEmpty()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_EMPTY)
            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).hasSize(2)
            }.assertSubstrings(Summaries.EXPECTED_COMPARISON)

            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).contains(4.0)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).doesNotContain(2.0)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 1.0)).hasNoDuplicates()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NO_DUPLICATES)

            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsAnyIn(4.0, 5.0, 6.0)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsAllIn(3.0, 2.0, 4.0)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsAllIn(3.0, 2.0, 1.0).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsNoneIn(3.0, 4.0, 5.0)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS)

            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsExactly(2.0, 2.0, 3.0)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsExactly(3.0, 2.0, 1.0).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)
            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 1.0, 2.0, 3.0)).containsExactly(1.0, 2.0, 3.0)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 1.0, 2.0, 3.0)).containsExactly(1.0, 2.0, 2.0, 3.0)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)

            // Verify inOrder doesn't get tripped up by duplicate elements in the list
            assertThrows<ReportError> {
                assertThat(doubleArrayOf(3.0, 2.0, 1.0, 2.0)).containsExactly(3.0, 2.0, 2.0, 1.0).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)

            // Verify inOrder doesn't get triggered if the original assert already failed
            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsAllIn(4.0, 5.0, 6.0).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
            assertThrows<ReportError> {
                assertThat(doubleArrayOf(1.0, 2.0, 3.0)).containsExactly(3.0).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS)
        }
    }
}

