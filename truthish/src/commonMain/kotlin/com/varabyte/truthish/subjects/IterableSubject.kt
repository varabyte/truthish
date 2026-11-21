package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Reportable
import com.varabyte.truthish.failure.Summaries

@Suppress("NAME_SHADOWING")
open class IterableSubject<T>(actual: Iterable<T>) : NotNullSubject<Iterable<T>>(actual) {
    private val actual = actual.toList()

    fun isEmpty() {
        if (actual.isNotEmpty()) {
            report(Report(Summaries.EXPECTED_COLLECTION_EMPTY, DetailsFor.actual(actual)))
        }
    }

    fun isNotEmpty() {
        if (actual.isEmpty()) {
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
        val other = other.toList()
        if (other.isEmpty()) {
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
        val other = other.toList()

        if (!actual.containsAll(other)) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS,
                    DetailsFor.expectedActual("all elements from", other, actual).apply {
                        add("Missing" to (other - actual))
                    }
                )
            )
            return skipInOrderCheck(this)
        }
        return OrderedAsserter(this, actual, other)
    }

    fun containsAllIn(vararg elements: T) = containsAllIn(elements.asIterable())

    fun containsNoneIn(other: Iterable<T>) {
        val other = other.toList()

        val commonElements = actual.intersect(other)
        if (commonElements.isNotEmpty()) {
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
        val other = other.toList()

        val remainingActual = actual.toMutableList()
        val remainingOther = other.toMutableList()
        other.forEach { remainingActual.remove(it) }
        actual.forEach { remainingOther.remove(it) }

        if (remainingActual.isNotEmpty() || remainingOther.isNotEmpty()) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS,
                    DetailsFor.expectedActual("exactly all elements from", other, actual).apply {
                        if (remainingOther.isNotEmpty()) {
                            add("Missing" to remainingOther)
                        }
                        if (remainingActual.isNotEmpty()) {
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

fun <T> IterableSubject<T>.containsAnyIn(other: Array<T>) = containsAnyIn(*other)
fun <T> IterableSubject<T>.containsAllIn(other: Array<T>) = containsAllIn(*other)
fun <T> IterableSubject<T>.containsNoneIn(other: Array<T>) = containsNoneIn(*other)
fun <T> IterableSubject<T>.containsExactly(other: Array<T>) = containsExactly(*other)


/**
 * We don't want to test inorder if a check already failed, so provide this [OrderedAsserter]\
 * instead, which is guaranteed to pass.
 */
internal fun <T> skipInOrderCheck(parent: Reportable) = OrderedAsserter<T>(parent, emptyList(), emptyList())

class OrderedAsserter<T>(
    private val parent: Reportable,
    actual: Iterable<T>,
    other: Iterable<T>
) {
    private val actual = actual.toList()
    private val other = other.toList()

    /**
     * It's possible that our lists might have duplicate values - make sure we don't ever recount the
     * same element in order!
     */
    private val usedIndices = mutableSetOf<Int>()

    fun inOrder() {
        var actualIndex = 0
        var otherIndex = 0

        while (otherIndex < other.size) {
            val lookingFor = other[otherIndex]
            while (actualIndex < actual.size) {
                if (!usedIndices.contains(actualIndex) && actual[actualIndex] == lookingFor) {
                    usedIndices.add(actualIndex)
                    break
                }
                ++actualIndex
            }
            if (actualIndex == actual.size) {
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
