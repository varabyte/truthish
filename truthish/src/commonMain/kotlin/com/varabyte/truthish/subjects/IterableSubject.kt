package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.Anchor
import com.varabyte.truthish.failure.Details
import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Reportable
import com.varabyte.truthish.failure.StringifierContext
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.Truncation

// Never truncate values that made an assertion fail, since they're what the user needs to see
internal val FailureValueContext = StringifierContext(Truncation.None)

@Suppress("NAME_SHADOWING")
open class IterableSubject<T>(actual: Iterable<T>) : NotNullSubject<Iterable<T>>(actual) {
    private val actual = IterableSnapshot(actual)

    fun isEmpty() {
        if (actual.list.isNotEmpty()) {
            report(Report(Summaries.EXPECTED_COLLECTION_EMPTY, DetailsFor.actual(actual.snapshot)))
        }
    }

    fun isNotEmpty() {
        if (actual.list.isEmpty()) {
            report(Report(Summaries.EXPECTED_COLLECTION_NOT_EMPTY))
        }
    }

    fun hasSize(size: Int) {
        val actualSize = actual.list.count()
        if (actualSize != size) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_SIZE,
                    DetailsFor.expectedActual(size, actualSize).apply { add(DetailsFor.VALUE to actual.snapshot) }
                )
            )
        }
    }

    fun contains(element: T) {
        if (!actual.list.contains(element)) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS,
                    DetailsFor.expectedActual(
                        "to contain",
                        element,
                        actual.snapshot,
                        expectedContext = FailureValueContext,
                    )
                )
            )
        }
    }

    fun doesNotContain(element: T) {
        if (actual.list.contains(element)) {
            val foundAtContext =
                StringifierContext(Truncation.Anchored(Anchor.CenteredOn(actual.list.indexOf(element))))
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                    DetailsFor.expectedActual(
                        "not to contain",
                        element,
                        actual.snapshot,
                        expectedContext = FailureValueContext,
                        actualContext = foundAtContext,
                    )
                )
            )
        }
    }

    fun hasNoDuplicates() {
        val duplicates = actual.list.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            // Show both copies of the first duplicate as an example. Others, if any, are still listed in full under
            // Duplicates.
            val seen = mutableSetOf<T>()
            // `add` returns false for an element that was already seen
            val repeatIndex = actual.list.indexOfFirst { !seen.add(it) }
            val originalIndex = actual.list.indexOf(actual.list[repeatIndex])
            val firstDuplicateContext = StringifierContext(
                Truncation.Anchored(Anchor.CenteredOn(originalIndex), Anchor.CenteredOn(repeatIndex))
            )
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_NO_DUPLICATES,
                    Details().apply {
                        add(DetailsFor.VALUE to actual.snapshot, firstDuplicateContext)
                        add(DetailsFor.DUPLICATES to duplicates.map { it.key }.toList(), FailureValueContext)
                    }
                )
            )
        }
    }

    fun containsAnyIn(other: Iterable<T>) {
        val other = IterableSnapshot(other)
        if (other.list.isEmpty()) {
            return
        }

        val intersection = actual.list.intersect(other.list)
        if (intersection.isEmpty()) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS,
                    DetailsFor.expectedActual("at least one element from", other.snapshot, actual.snapshot)
                )
            )
        }
    }

    fun containsAnyIn(vararg elements: T) = containsAnyIn(elements.asIterable())

    fun containsAllIn(other: Iterable<T>): OrderedAsserter<T> {
        val other = IterableSnapshot(other)

        // Each element of `actual` can only account for one expected element, so duplicates must all be there
        val missing = other.list.toMutableList()
        actual.list.forEach { missing.remove(it) }
        if (missing.isNotEmpty()) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS,
                    DetailsFor.expectedActual("all elements from", other.snapshot, actual.snapshot).apply {
                        add(DetailsFor.MISSING to missing, FailureValueContext)
                    }
                )
            )
            return skipInOrderCheck(this)
        }
        return OrderedAsserter(this, actual.snapshot, other.snapshot)
    }

    fun containsAllIn(vararg elements: T) = containsAllIn(elements.asIterable())

    fun containsNoneIn(other: Iterable<T>) {
        val other = IterableSnapshot(other)

        val commonElements = actual.list.intersect(other.list).toList()
        if (commonElements.isNotEmpty()) {
            // `intersect` keeps the order of `actual`, so its first element is the first one found there. Both lists
            // are centered on their copy of it.
            val firstSharedElement = commonElements.first()
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                    DetailsFor.expectedActual(
                        "no elements from",
                        other.snapshot,
                        actual.snapshot,
                        expectedContext = other.list.indexOf(firstSharedElement).toCenteredContext(),
                        actualContext = actual.list.indexOf(firstSharedElement).toCenteredContext(),
                    ).apply {
                        add(DetailsFor.UNEXPECTED to commonElements, FailureValueContext)
                    }
                )
            )
        }
    }

    fun containsNoneIn(vararg elements: T) = containsNoneIn(elements.asIterable())

    fun containsExactly(other: Iterable<T>): OrderedAsserter<T> {
        val other = IterableSnapshot(other)

        val remainingActual = actual.list.toMutableList()
        val remainingOther = other.list.toMutableList()
        other.list.forEach { remainingActual.remove(it) }
        actual.list.forEach { remainingOther.remove(it) }

        if (remainingActual.isNotEmpty() || remainingOther.isNotEmpty()) {
            report(
                Report(
                    Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
                    DetailsFor.expectedActual(
                        "exactly all elements from",
                        other.snapshot,
                        actual.snapshot,
                        expectedContext = other.list.anchorOnFirstUnmatched(actual.list),
                        actualContext = actual.list.anchorOnFirstUnmatched(other.list),
                    ).apply {
                        if (remainingOther.isNotEmpty()) {
                            add(DetailsFor.MISSING to remainingOther, FailureValueContext)
                        }
                        if (remainingActual.isNotEmpty()) {
                            add(DetailsFor.EXTRANEOUS to remainingActual, FailureValueContext)
                        }
                    }
                )
            )
            return skipInOrderCheck(this)
        }

        return OrderedAsserter(this, actual.snapshot, other.snapshot)
    }

    fun containsExactly(vararg elements: T) = containsExactly(elements.asIterable())
}

/**
 * Creates a context centered on the first element of this list that [other] has no copy of.
 *
 * If that element appears more than once in this list, any copy could be the unmatched one, so the context shows the
 * first two copies instead.
 */
private fun <T> List<T>.anchorOnFirstUnmatched(other: List<T>): StringifierContext {
    /**
     * Finds the first element of this list that [other] has no copy of, e.g. the first missing or extraneous element.
     *
     * Each element of [other] matches at most one element of this list, so a duplicate beyond the copies in [other]
     * counts as unmatched.
     */
    fun List<T>.findFirstUnmatchedIndex(other: List<T>): Int? {
        val unmatchedCounts = other.groupingBy { it }.eachCount().toMutableMap()
        return indexOfFirst { element ->
            val count = unmatchedCounts[element] ?: 0
            unmatchedCounts[element] = count - 1
            count <= 0
        }.takeIf { it >= 0 }
    }

    val index = findFirstUnmatchedIndex(other) ?: return StringifierContext.Default
    val copyIndices = indices.filter { this[it] == this[index] }
    if (copyIndices.size < 2) return index.toCenteredContext()
    return StringifierContext(Truncation.Anchored(Anchor.CenteredOn(copyIndices[0]), Anchor.CenteredOn(copyIndices[1])))
}

/**
 * A copy of an iterable, read only once, since some can't be read twice (e.g. a sequence).
 *
 * The copy also guards against the original changing before a deferred report renders it.
 */
private class IterableSnapshot<T>(iterable: Iterable<T>) {
    /** The copy to show in reports. A set stays a set, so it renders as `{ … }`. */
    val snapshot: Iterable<T>

    /** The same elements, in the same order, exposed as a list, useful for checking assertions. */
    val list: List<T>

    init {
        if (iterable is Set<T>) {
            snapshot = iterable.toSet()
            list = snapshot.toList()
        } else {
            list = iterable.toList()
            snapshot = list
        }
    }
}

// Centers a long list on this int value (which should be an index), or keeps both ends if there's nothing to center on
private fun Int?.toCenteredContext(): StringifierContext {
    val index = this ?: return StringifierContext.Default
    return StringifierContext(Truncation.Anchored(Anchor.CenteredOn(index)))
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
    private val actual = IterableSnapshot(actual)
    private val other = IterableSnapshot(other)

    /**
     * It's possible that our lists might have duplicate values - make sure we don't ever recount the
     * same element in order!
     */
    private val usedIndices = mutableSetOf<Int>()

    fun inOrder() {
        var actualIndex = 0
        var otherIndex = 0
        var lastMatchIndex: Int? = null

        while (otherIndex < other.list.size) {
            val lookingFor = other.list[otherIndex]
            while (actualIndex < actual.list.size) {
                if (!usedIndices.contains(actualIndex) && actual.list[actualIndex] == lookingFor) {
                    usedIndices.add(actualIndex)
                    lastMatchIndex = actualIndex
                    break
                }
                ++actualIndex
            }
            if (actualIndex == actual.list.size) {
                // If we got here, we couldn't find the next element from [other]
                // Where the element sits in the actual list, which is before where the elements so far were found
                val foundIndex =
                    actual.list.indices.firstOrNull { !usedIndices.contains(it) && actual.list[it] == lookingFor }
                // Show the element next to the previous match, which it was expected to come after
                val actualContext = if (foundIndex != null && lastMatchIndex != null) {
                    StringifierContext(
                        Truncation.Anchored(Anchor.CenteredOn(foundIndex), Anchor.CenteredOn(lastMatchIndex))
                    )
                } else {
                    foundIndex.toCenteredContext()
                }
                parent.report(
                    Report(
                        Summaries.EXPECTED_COLLECTION_ORDERED,
                        DetailsFor.expectedActual(
                            "in order",
                            other.snapshot,
                            actual.snapshot,
                            expectedContext = otherIndex.toCenteredContext(),
                            actualContext = actualContext,
                        ).apply {
                            add(DetailsFor.OUT_OF_ORDER to lookingFor, FailureValueContext)
                        }
                    )
                )
                break
            }

            ++otherIndex
        }

        // If we got here -- congrats! All elements of [other] were found in [actual] in order.
    }
}
