package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Summaries

private fun <K, V> Map<K, V>.toIterable(): Iterable<Pair<K, V>> = entries.map { it.toPair() }

/**
 * A subject useful for testing the state of a [Map]'s entries.
 *
 * Note that if you want to make assertions about a map's keys or values, you can assert against
 * them directly:
 *
 * ```
 * val asciiMap: Map<Char, Int> = ... // mapping of 'a-z' to their ascii codes
 *
 * assertThat(map).contains('e' to 101)
 * assertThat(map).containsKey('e')
 * assertThat(map).containsValue(101)
 * assertThat(map.keys).contains('e')
 * assertThat(map.values).contains(101)
 *
 * assertThat(map).containsAllIn('a' to 97, 'z' to 122)
 * assertThat(map.keys).containsAllIn('a', 'z')
 * assertThat(map.values).containsAllIn(97, 122)
 * ```
 */
class MapSubject<K, V>(private val actual: Map<K, V>) : IterableSubject<Pair<K, V>>(actual.toIterable()) {
    fun isEqualTo(expected: Map<K, V>) {
        if (actual != expected) {
            report(Report(Summaries.EXPECTED_EQUAL, DetailsFor.expectedActual(expected, actual)))
        }
    }

    fun isNotEqualTo(expected: Map<K, V>) {
        if (actual == expected) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.expected(actual)))
        }
    }

    fun containsKey(key: K) {
        if (!actual.containsKey(key)) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS,
                    DetailsFor.expectedActual("to contain key", key, actual.keys)
                )
            )
        }
    }

    fun containsValue(value: V) {
        if (!actual.containsValue(value)) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS,
                    DetailsFor.expectedActual("to contain value", value, actual.values)
                )
            )
        }
    }

    fun doesNotContainKey(key: K) {
        if (actual.containsKey(key)) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                    DetailsFor.expectedActual("not to contain key", key, actual.keys)
                )
            )
        }
    }

    fun doesNotContainValue(value: V) {
        if (actual.containsValue(value)) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                    DetailsFor.expectedActual("not to contain value", value, actual.values)
                )
            )
        }
    }

    fun containsAnyIn(other: Map<K, V>) = containsAnyIn(other.toIterable())
    fun containsAllIn(other: Map<K, V>) = containsAllIn(other.toIterable())
    fun containsNoneIn(other: Map<K, V>) = containsNoneIn(other.toIterable())
    fun containsExactly(other: Map<K, V>) = containsExactly(other.toIterable())
}
