package com.varabyte.truthish

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import kotlin.test.Test

class MapAsserts {
    @Test
    fun mapChecks() {
        val asciiMap = ('a'..'z').associateWith { it.code }

        run {
            assertThat(asciiMap).isEqualTo(asciiMap.toMap())
            assertThat(asciiMap).isNotEqualTo(emptyMap())

            assertThat(asciiMap).containsKey('g')
            assertThat(asciiMap.keys).contains('g')
            assertThat(asciiMap).doesNotContainKey('G')
            assertThat(asciiMap.keys).doesNotContain('G')
            assertThat(asciiMap).containsValue(107)
            assertThat(asciiMap.values).contains(107)
            assertThat(asciiMap).doesNotContainValue(9999)
            assertThat(asciiMap.values).doesNotContain(9999)
            assertThat(asciiMap).contains('e' to 101)
            assertThat(asciiMap).doesNotContain('e' to 102)

            assertThat(asciiMap).containsAnyIn('a' to 97, 'a' to 98)
            assertThat(asciiMap).containsAllIn('a' to 97, 'z' to 122).inOrder()
            assertThat(asciiMap).containsNoneIn('a' to 122, 'z' to 97)
            assertThat(asciiMap).containsAnyIn(mapOf('a' to 97, '?' to 9999))
            assertThat(asciiMap).containsAllIn(mapOf('a' to 97, 'z' to 122)).inOrder()
            assertThat(asciiMap).containsNoneIn(mapOf('a' to 122, 'z' to 97))

            assertThat(asciiMap).containsExactly(asciiMap.toMap())
        }

        run {
            // Test false statements
            assertThrows<AssertionError> {
                assertThat(asciiMap).isEqualTo(emptyMap())
            }.assertSubstrings(Summaries.EXPECTED_EQUAL, DetailsFor.BUT_WAS, "{ 'a' to 97, 'b' to 98")

            assertThrows<AssertionError> {
                assertThat(asciiMap).isNotEqualTo(asciiMap)
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.VALUE)

            assertThrows<AssertionError> {
                assertThat(asciiMap).doesNotContainKey('g')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS, "'g'")

            assertThrows<AssertionError> {
                assertThat(asciiMap.keys).doesNotContain('g')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS, "'g'")

            assertThrows<AssertionError> {
                assertThat(asciiMap).containsKey('G')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, "'G'")

            assertThrows<AssertionError> {
                assertThat(asciiMap.keys).contains('G')
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, "'G'")

            assertThrows<AssertionError> {
                assertThat(asciiMap).doesNotContainValue(107)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS, "107")

            assertThrows<AssertionError> {
                assertThat(asciiMap.values).doesNotContain(107)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS, "107")

            assertThrows<AssertionError> {
                assertThat(asciiMap).containsValue(9999)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, "9999")

            assertThrows<AssertionError> {
                assertThat(asciiMap.values).contains(9999)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, "9999")

            assertThrows<AssertionError> {
                assertThat(asciiMap).doesNotContain('e' to 101)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_NOT_CONTAINS, "'e' to 101")

            assertThrows<AssertionError> {
                assertThat(asciiMap).contains('e' to 102)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, "'e' to 102")

            assertThrows<AssertionError> {
                assertThat(asciiMap).containsAllIn('a' to 122, 'z' to 122)
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_CONTAINS, DetailsFor.MISSING, "[ 'a' to 122 ]")

            assertThrows<AssertionError> {
                assertThat(asciiMap).containsAllIn('z' to 122, 'a' to 97).inOrder()
            }.assertSubstrings(Summaries.EXPECTED_COLLECTION_ORDERED)
        }
    }

    @Test
    fun longMapsAreTruncatedAroundTheElementThatCausedTheFailure() {
        val tens = (1..30).associateWith { it * 10 }

        assertThrows<AssertionError> {
            assertThat(tens).doesNotContainKey(16)
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
            DetailsFor.BUT_WAS, "{ … (10 more), 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, … (9 more) }",
        )

        assertThrows<AssertionError> {
            assertThat(tens).doesNotContainValue(160)
        }.assertSubstrings(
            Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
            DetailsFor.BUT_WAS, "[ … (10 more), 110, 120, 130, 140, 150, 160, 170, 180, 190, 200, 210, … (9 more) ]",
        )
    }
}