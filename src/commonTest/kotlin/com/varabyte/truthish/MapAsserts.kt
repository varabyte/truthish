package com.varabyte.truthish

import com.varabyte.truthish.failure.ReportError
import kotlin.test.Test

class MapAsserts {
    fun <K, V> Map<K, V>.reversed(): Map<K, V> {
        return this.entries.reversed().associate { it.toPair() }
    }

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
            assertThat(asciiMap).containsAnyIn(mapOf('a' to 97, '?' to 9999))
            assertThat(asciiMap).containsAllIn('z' to 122, 'a' to 97)
            assertThat(asciiMap).containsAllIn('a' to 97, 'z' to 122).inOrder()
            assertThat(asciiMap).containsAllIn(mapOf('z' to 122, 'a' to 97))
            assertThat(asciiMap).containsAllIn(mapOf('a' to 97, 'z' to 122)).inOrder()
            assertThat(asciiMap).containsNoneIn('a' to 122, 'z' to 97)
            assertThat(asciiMap).containsNoneIn(mapOf('a' to 122, 'z' to 97))

            assertThat(asciiMap).containsExactly(asciiMap.toMap().reversed())
            assertThat(asciiMap).containsExactly(asciiMap.toMap()).inOrder()
        }

        run {
            // Test false statements
            assertThrows<ReportError> {
                assertThat(asciiMap).isEqualTo(emptyMap())
            }

            assertThrows<ReportError> {
                assertThat(asciiMap).isNotEqualTo(asciiMap)
            }

            assertThrows<ReportError> {
                assertThat(asciiMap).doesNotContainKey('g')
            }

            assertThrows<ReportError> {
                assertThat(asciiMap.keys).doesNotContain('g')
            }

            assertThrows<ReportError> {
                assertThat(asciiMap).containsKey('G')
            }

            assertThrows<ReportError> {
                assertThat(asciiMap.keys).contains('G')
            }

            assertThrows<ReportError> {
                assertThat(asciiMap).doesNotContainValue(107)
            }

            assertThrows<ReportError> {
                assertThat(asciiMap.values).doesNotContain(107)
            }

            assertThrows<ReportError> {
                assertThat(asciiMap).containsValue(9999)
            }

            assertThrows<ReportError> {
                assertThat(asciiMap.values).contains(9999)
            }

            assertThrows<ReportError> {
                assertThat(asciiMap).doesNotContain('e' to 101)
            }

            assertThrows<ReportError> {
                assertThat(asciiMap).contains('e' to 102)
            }

            assertThrows<ReportError> {
                assertThat(asciiMap).containsAllIn('a' to 122, 'z' to 122)
            }

            assertThrows<ReportError> {
                assertThat(asciiMap).containsAllIn('z' to 122, 'a' to 97).inOrder()
            }

            assertThrows<ReportError> {
                assertThat(asciiMap).containsExactly('z' to 122, 'a' to 97)
            }

            assertThrows<ReportError> {
                assertThat(asciiMap).containsExactly(asciiMap.reversed()).inOrder()
            }
        }
    }
}