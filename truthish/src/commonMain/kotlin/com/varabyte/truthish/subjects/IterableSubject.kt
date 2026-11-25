package com.varabyte.truthish.subjects

import com.varabyte.truthish.Correspondence
import com.varabyte.truthish.failure.AnyStringifier
import com.varabyte.truthish.failure.Details
import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.StringifierContext
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.Truncators
import com.varabyte.truthish.util.truncate.Truncator
import com.varabyte.truthish.util.truncate.Anchor
import com.varabyte.truthish.util.truncate.Truncation

// Never truncate values that made an assertion fail, since they're what the user needs to see
internal val FailureValueContext = StringifierContext(Truncation.None)

@Suppress("NAME_SHADOWING")
open class IterableSubject<T>(actual: Iterable<T>) : NotNullSubject<Iterable<T>>(actual) {
    private val actual = IterableSnapshot(actual)

    private val asserter = ContainmentAsserter<T, T>(this, this.actual, ElementComparison.Equality)

    /**
     * Details for two unequal lists, both truncated around where they first differ, plus the elements each one lacks.
     */
    protected fun DetailsFor.expectedActualLists(expected: List<*>, actual: List<*>): Details {
        val context = expected.createSharedContext(actual, Truncators.forCollections)
        return expectedActual(expected, actual, expectedContext = context, actualContext = context)
            .apply { addElementDifferences(expected, actual) }
    }

    /** Details for two unequal sets, plus the elements each one lacks. */
    protected fun DetailsFor.expectedActualSets(expected: Set<*>, actual: Set<*>): Details {
        return expectedActual(expected, actual).apply { addElementDifferences(expected, actual) }
    }

    /**
     * Like [AnySubject.isEqualTo], except two lists or two sets also list the elements each one lacks, and two long
     * lists are truncated around where they first differ.
     */
    fun isEqualTo(expected: Iterable<*>?) {
        if (actual.original == expected) return
        val details = when {
            expected is List<*> && actual.original is List<*> -> DetailsFor.expectedActualLists(expected, actual.original)
            expected is Set<*> && actual.original is Set<*> -> DetailsFor.expectedActualSets(expected, actual.original)
            // Use `snapshot` here, as in the else branch we could possibly be dealing with a one-shot sequence.
            else -> DetailsFor.expectedActual(expected, actual.snapshot)
        }
        report(Report(Summaries.EXPECTED_EQUAL, details))
    }

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

    fun contains(element: T) = asserter.contains(element)

    fun doesNotContain(element: T) = asserter.doesNotContain(element)

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

    fun containsAnyIn(other: Iterable<T>) = asserter.containsAnyIn(other)

    fun containsAnyIn(vararg elements: T) = containsAnyIn(elements.asIterable())

    fun containsAllIn(other: Iterable<T>): OrderedAsserter = asserter.containsAllIn(other)

    fun containsAllIn(vararg elements: T) = containsAllIn(elements.asIterable())

    fun containsNoneIn(other: Iterable<T>) = asserter.containsNoneIn(other)

    fun containsNoneIn(vararg elements: T) = containsNoneIn(elements.asIterable())

    fun containsExactly(other: Iterable<T>): OrderedAsserter = asserter.containsExactly(other)

    fun containsExactly(vararg elements: T) = containsExactly(elements.asIterable())

    /**
     * Register a [correspondence] which allows transforming this collection's elements before comparing them.
     *
     * For example:
     * ```
     * assertThat(errors)
     *     .comparingElementsUsing(Correspondence.contains)
     *     .containsExactly("unresolved", "cannot be reassigned")
     * ```
     */
    fun <E> comparingElementsUsing(correspondence: Correspondence<T, E>) =
        CorrespondenceSubject(this, actual, correspondence)
}

/**
 * Creates a context centered on the element at [index], or one that keeps both ends if it's `null`.
 *
 * If that element appears more than once in this list, any copy could be the one that matters (e.g. an unmatched
 * duplicate), so the context shows the first two copies instead.
 */
internal fun <T> List<T>.anchorOnCopiesOf(index: Int?): StringifierContext {
    if (index == null) return StringifierContext.Default
    val copyIndices = indices.filter { this[it] == this[index] }
    if (copyIndices.size < 2) return index.toCenteredContext()
    return StringifierContext(Truncation.Anchored(Anchor.CenteredOn(copyIndices[0]), Anchor.CenteredOn(copyIndices[1])))
}

/**
 * A copy of an iterable, read only once, since some can't be read twice (e.g. a sequence).
 *
 * The copy also guards against the original changing before a deferred report renders it.
 */
internal class IterableSnapshot<T>(iterable: Iterable<T>) {
    /**
     * The original iterable itself, kept around for equality checks.
     *
     * For example, a range equals another range with the same bounds, but not a list with the same elements.
     */
    val original = iterable

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

/**
 * A context for this list and [other], two lists (or, for strings, character lists) being compared, that truncates both
 * around where they first differ, with their shown elements lined up.
 */
internal fun List<*>.createSharedContext(other: List<*>, truncator: Truncator): StringifierContext {
    val shorterSize = minOf(size, other.size)
    val firstDifference = (0 until shorterSize).firstOrNull { this[it] != other[it] }
        ?: shorterSize.takeIf { size != other.size }
        ?: return StringifierContext.Default

    val anchor = when {
        // A list that fits is never truncated, so the other list starts where it does, to line up
        shorterSize <= truncator.maxElements -> Anchor.CenteredOn(0)
        // One list starts with the other, so show where the shorter one ends
        firstDifference == shorterSize -> Anchor.EndingAt(firstDifference)
        else -> Anchor.CenteredOn(firstDifference, listSizeOverride = maxOf(size, other.size))
    }
    return StringifierContext(Truncation.Anchored(anchor))
}

// Centers a long list on this int value (which should be an index), or keeps both ends if there's nothing to center on
internal fun Int?.toCenteredContext(): StringifierContext {
    val index = this ?: return StringifierContext.Default
    return StringifierContext(Truncation.Anchored(Anchor.CenteredOn(index)))
}

/** The elements of this iterable that [other] lacks, considering duplicates, so `[a, a, b]` minus `[a]` is `[a, b]`. */
private fun Iterable<*>.minusEach(other: Iterable<*>): List<*> {
    val counts = other.groupingBy { it }.eachCount().toMutableMap()
    return filter { element ->
        val count = counts[element] ?: 0
        if (count > 0) counts[element] = count - 1
        count == 0
    }
}

/**
 * Adds the elements [expected] has that [actual] lacks and vice versa, since truncating the two values can hide every
 * difference but the first. If neither lacks anything, they must differ in order, so a note says so instead.
 */
private fun Details.addElementDifferences(expected: Iterable<*>, actual: Iterable<*>) {
    val missing = expected.minusEach(actual)
    val extraneous = actual.minusEach(expected)
    if (missing.isEmpty() && extraneous.isEmpty()) {
        add(DetailsFor.NOTE to AnyStringifier("Same elements, in a different order"))
        return
    }
    if (missing.isNotEmpty()) add(DetailsFor.MISSING to missing, FailureValueContext)
    if (extraneous.isNotEmpty()) add(DetailsFor.EXTRANEOUS to extraneous, FailureValueContext)
}

fun <T> IterableSubject<T>.containsAnyIn(other: Array<T>) = containsAnyIn(*other)
fun <T> IterableSubject<T>.containsAllIn(other: Array<T>) = containsAllIn(*other)
fun <T> IterableSubject<T>.containsNoneIn(other: Array<T>) = containsNoneIn(*other)
fun <T> IterableSubject<T>.containsExactly(other: Array<T>) = containsExactly(*other)

/**
 * Like [IterableSubject.comparingElementsUsing], for a one-off correspondence written inline.
 *
 * ```
 * assertThat(words)
 *     .comparingElementsUsing("has length") { word, length: Int -> word.length == length }
 *     .containsExactly(3, 5)
 * ```
 *
 * @param description What an actual element does to the expected element it corresponds to, e.g. "has length".
 *   See [Correspondence] for how it's used.
 */
fun <T, E> IterableSubject<T>.comparingElementsUsing(
    description: String,
    predicate: (actual: T, expected: E) -> Boolean,
) = comparingElementsUsing(Correspondence(description, predicate))

/**
 * Lets an assertion that passed also check that elements appear in the expected order, via [inOrder].
 */
class OrderedAsserter internal constructor(private val checkInOrder: () -> Unit) {
    fun inOrder() = checkInOrder()
}
