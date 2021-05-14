package com.github.bitspittle.truthish.subjects

import com.github.bitspittle.truthish.failure.DetailsFor
import com.github.bitspittle.truthish.failure.Report
import com.github.bitspittle.truthish.failure.Summaries

open class IterableSubject<T>(private val actual: Iterable<T>) : NotNullSubject<Iterable<T>>(actual) {
    fun isEmpty() {
        if (actual.count() != 0) {
            report(Report(Summaries.EXPECTED_COLLECTION_EMPTY, DetailsFor.actual(actual)))
        }
    }

    fun isNotEmpty() {
        if (actual.count() == 0) {
            report(Report(Summaries.EXPECTED_COLLECTION_NOT_EMPTY))
        }
    }

    fun hasSize(size: Int) {
        val actualSize = actual.count()
        if (actualSize != size) {
            report(Report(Summaries.EXPECTED_COMPARISON, DetailsFor.expectedActual("size", size, actualSize)))
        }
    }

    fun contains(element: T) {
        if (!actual.contains(element)) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS,
                    DetailsFor.expectedActual("to contain", element, actual)
                )
            )
        }
    }

    fun doesNotContain(element: T) {
        if (actual.contains(element)) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                    DetailsFor.expectedActual("not to contain", element, actual)
                )
            )
        }
    }

    fun hasNoDuplicates() {
        val duplicates = actual.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_NO_DUPLICATES,
                    DetailsFor.actual(actual).apply {
                        add("Duplicates" to duplicates.map { it.key }.toList())
                    }
                )
            )
        }
    }

    fun containsAnyIn(other: Iterable<T>) {
        if (other.count() == 0) {
            return
        }

        val intersection = actual.intersect(other)
        if (intersection.isEmpty()) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS,
                    DetailsFor.expectedActual("at least one element from", other, actual)
                )
            )
        }
    }

    fun containsAnyIn(vararg elements: T) = containsAnyIn(elements.asIterable())

    fun containsAllIn(other: Iterable<T>): OrderedAsserter<T> {
        val actualList = actual.toList()
        val otherList = other.toList()

        if (!actualList.containsAll(otherList)) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS,
                    DetailsFor.expectedActual("all elements from", other, actual).apply {
                        add("Missing" to (otherList - actualList))
                    }
                )
            )
            return skipInOrderCheck(this)
        }
        return OrderedAsserter(this, actual, other)
    }

    fun containsAllIn(vararg elements: T) = containsAllIn(elements.asIterable())

    fun containsNoneIn(other: Iterable<T>) {
        val commonElements = actual.intersect(other)
        if (!commonElements.isEmpty()) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                    DetailsFor.expectedActual("no elements from", other, actual).apply {
                        add("Containing" to commonElements)
                    }
                )
            )
        }
    }

    fun containsNoneIn(vararg elements: T) = containsNoneIn(elements.asIterable())

    fun containsExactly(other: Iterable<T>): OrderedAsserter<T> {
        val remainingActual = actual.toMutableList()
        val remainingOther = other.toMutableList()
        other.forEach { remainingActual.remove(it) }
        actual.forEach { remainingOther.remove(it) }

        if (remainingActual.isNotEmpty() || remainingOther.isNotEmpty()) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS,
                    DetailsFor.expectedActual("exactly all elements from", other, actual).apply {
                        if (!remainingOther.isEmpty()) {
                            add("Missing" to remainingOther)
                        }
                        if (!remainingActual.isEmpty()) {
                            add("Extraneous" to remainingActual)
                        }
                    }
                )
            )
            return skipInOrderCheck(this)
        }

        return OrderedAsserter(this, actual, other)
    }

    fun containsExactly(vararg elements: T) = containsExactly(elements.asIterable())
}

/**
 * We don't want to test inorder if a check already failed, so provide this [OrderedAsserter]\
 * instead, which is guaranteed to pass.
 */
private fun <T> skipInOrderCheck(parent: IterableSubject<T>) = OrderedAsserter(parent, listOf(), listOf())

class OrderedAsserter<T>(
    private val parent: IterableSubject<T>,
    private val actual: Iterable<T>,
    private val other: Iterable<T>
) {
    fun inOrder() {
        var actualIndex = 0
        var otherIndex = 0
        val actualList = actual.toList()
        val otherList = other.toList()

        while (otherIndex < otherList.size) {
            val lookingFor = otherList[otherIndex]
            while (actualIndex < actualList.size) {
                if (actualList[actualIndex] == lookingFor) {
                    break
                }
                ++actualIndex
            }
            if (actualIndex == actualList.size) {
                // If we got here, we couldn't find the next element from [other]
                parent.report(
                    Report(
                        Summaries.EXPECTED_COLLECTION_ORDERED,
                        DetailsFor.expectedActual("in order", other, actual)
                    )
                )
                break
            }

            ++otherIndex
        }

        // If we got here -- congrats! All elements of [other] were found in [actual] in order.
    }
}

/**
 * A subject useful for testing the state of a [Map]'s entries.
 *
 * Note that if you want to make assertions about a map's keys or values, you should assert against
 * them directly:
 *
 * ```
 * val asciiMap: Map<Char, Int> = ... // mapping of 'a-z' to their ascii codes
 *
 * assertThat(map).contains('e' to 101)
 * assertThat(map.keys).contains('e')
 * assertThat(map.values).contains(101)
 *
 * assertThat(map).containsAllIn('a' to 97, 'z' to 122)
 * assertThat(map.keys).containsAllIn('a', 'z')
 * assertThat(map.values).containsAllIn(97, 122)
 * ```
 */
class MapSubject<K, V>(actual: Map<K, V>) :
    IterableSubject<Pair<K, V>>(actual.entries.map { it.toPair() }) {
}
