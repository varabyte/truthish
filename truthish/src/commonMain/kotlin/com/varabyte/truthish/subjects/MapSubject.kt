package com.varabyte.truthish.subjects

import com.varabyte.truthish.Correspondence
import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.StringifierContext
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.util.truncate.Anchor
import com.varabyte.truthish.util.truncate.Truncation

// Return as a set, so reports show the entries the way a map is shown, e.g. `{ "a" to 1 }`
private fun <K, V> Map<K, V>.toIterable(): Iterable<Pair<K, V>> = entries.map { it.toPair() }.toSet()

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
            val expectedEntries = expected.toList()
            val actualEntries = actual.toList()
            val missing = expectedEntries - actualEntries.toSet()
            val extraneous = actualEntries - expectedEntries.toSet()
            // Like `containsExactly`, each side centers on its own first entry that the other side lacks
            val firstMissingIndex = missing.firstOrNull()?.let { expectedEntries.indexOf(it) }
            val firstExtraneousIndex = extraneous.firstOrNull()?.let { actualEntries.indexOf(it) }
            report(
                Report(
                    Summaries.EXPECTED_EQUAL,
                    DetailsFor.expectedActual(
                        expected,
                        actual,
                        expectedContext = firstMissingIndex.toCenteredOnContext(),
                        actualContext = firstExtraneousIndex.toCenteredOnContext(),
                    ).apply {
                        // Truncation can still hide later differences, so list them all too. A key in both maps with
                        // different values shows up in both rows.
                        if (missing.isNotEmpty()) add(DetailsFor.MISSING to missing.toMap(), KeepStartContext)
                        if (extraneous.isNotEmpty()) {
                            add(DetailsFor.EXTRANEOUS to extraneous.toMap(), KeepStartContext)
                        }
                    }
                )
            )
        }
    }

    fun isNotEqualTo(expected: Map<K, V>) {
        if (actual == expected) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.actual(actual)))
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
                    DetailsFor.expectedActual(
                        "to contain value",
                        value,
                        actual.values,
                    )
                )
            )
        }
    }

    fun doesNotContainKey(key: K) {
        if (actual.containsKey(key)) {
            val foundAtContext = StringifierContext(Truncation.Anchored(Anchor.CenteredOn(actual.keys.indexOf(key))))
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                    DetailsFor.expectedActual(
                        "not to contain key",
                        key,
                        actual.keys,
                        actualContext = foundAtContext,
                    )
                )
            )
        }
    }

    fun doesNotContainValue(value: V) {
        if (actual.containsValue(value)) {
            val foundAtContext = StringifierContext(
                Truncation.Anchored(Anchor.CenteredOn(actual.values.indexOf(value)))
            )
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                    DetailsFor.expectedActual(
                        "not to contain value",
                        value,
                        actual.values,
                        actualContext = foundAtContext,
                    )
                )
            )
        }
    }

    fun containsAnyIn(other: Map<K, V>) = containsAnyIn(other.toIterable())
    fun containsAllIn(other: Map<K, V>) = containsAllIn(other.toIterable())
    fun containsNoneIn(other: Map<K, V>) = containsNoneIn(other.toIterable())
    fun containsExactly(other: Map<K, V>) = containsExactly(other.toIterable())

    /**
     * Register a [correspondence] for comparing this map's values more loosely against expected values.
     *
     * Meanwhile, its keys are still compared with direct equality.
     *
     * For example:
     * ```
     * assertThat(prices)
     *     .comparingValuesUsing(Correspondence.within(0.1))
     *     .containsExactly(mapOf("apple" to 1.0, "pear" to 2.5))
     * ```
     * would allow the test to pass even if "apple" mapped to `1.05`.
     */
    fun <E> comparingValuesUsing(correspondence: Correspondence<V, E>) =
        CorrespondenceSubject(
            this,
            IterableSnapshot(actual.toIterable()),
            Correspondence<Pair<K, V>, Pair<K, E>>(correspondence.description) { actual, expected ->
                actual.first == expected.first && correspondence.compare(actual.second, expected.second)
            },
        )
}

/**
 * Like [MapSubject.comparingValuesUsing], for a one-off correspondence written inline.
 *
 * ```
 * assertThat(wordsByLength)
 *     .comparingValuesUsing("has size") { words, size: Int -> words.size == size }
 *     .containsExactly(mapOf(3 to 2, 5 to 1))
 * ```
 *
 * @param description What an actual value does to the expected value it corresponds to, e.g. "has size".
 *   See [Correspondence] for how it's used.
 */
fun <K, V, E> MapSubject<K, V>.comparingValuesUsing(
    description: String,
    predicate: (actual: V, expected: E) -> Boolean,
) = comparingValuesUsing(Correspondence(description, predicate))

fun <K, V, E> CorrespondenceSubject<Pair<K, V>, Pair<K, E>>.containsAnyIn(other: Map<K, E>) =
    containsAnyIn(other.toIterable())
fun <K, V, E> CorrespondenceSubject<Pair<K, V>, Pair<K, E>>.containsAllIn(other: Map<K, E>) =
    containsAllIn(other.toIterable())
fun <K, V, E> CorrespondenceSubject<Pair<K, V>, Pair<K, E>>.containsNoneIn(other: Map<K, E>) =
    containsNoneIn(other.toIterable())
fun <K, V, E> CorrespondenceSubject<Pair<K, V>, Pair<K, E>>.containsExactly(other: Map<K, E>) =
    containsExactly(other.toIterable())
