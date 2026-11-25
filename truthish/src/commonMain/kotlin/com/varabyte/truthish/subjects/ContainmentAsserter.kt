package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.AnyStringifier
import com.varabyte.truthish.failure.Details
import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Reportable
import com.varabyte.truthish.failure.StringifierContext
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.ValueStringifier
import com.varabyte.truthish.util.truncate.Anchor
import com.varabyte.truthish.util.truncate.Truncation

/** How actual elements are compared with expected ones. */
internal sealed interface ElementComparison<in A, in E> {
    fun matches(actual: A, expected: E): Boolean

    object Equality : ElementComparison<Any?, Any?> {
        override fun matches(actual: Any?, expected: Any?) = actual == expected
    }

    class Using<in A, in E>(private val predicate: (actual: A, expected: E) -> Boolean) : ElementComparison<A, E> {
        override fun matches(actual: A, expected: E) = predicate(actual, expected)
    }
}

/**
 * Checks for which elements a collection contains, shared by subjects that make these assertions so they all check and
 * report failures the same way.
 *
 * Failures are reported through [parent], so its message, name and failure strategy (e.g. inside `assertAll`) apply.
 *
 * @param interceptDetails Called with each failure's details before they're reported, e.g. to add a line.
 */
@Suppress("NAME_SHADOWING")
internal class ContainmentAsserter<A, E>(
    private val parent: Reportable,
    private val actual: IterableSnapshot<A>,
    private val comparison: ElementComparison<A, E>,
    private val interceptDetails: (Details) -> Unit = {},
) {
    fun contains(expected: E) {
        if (actual.list.none { comparison.matches(it, expected) }) {
            report(
                Summaries.EXPECTED_COLLECTION_CONTAINS,
                DetailsFor.expectedActual(
                    "to contain",
                    expected,
                    actual.snapshot,
                )
            )
        }
    }

    fun doesNotContain(expected: E) {
        val foundIndex = actual.list.indexOfFirst { comparison.matches(it, expected) }
        if (foundIndex >= 0) {
            report(
                Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                DetailsFor.expectedActual(
                    "not to contain",
                    expected,
                    actual.snapshot,
                    actualContext = foundIndex.toCenteredOnContext(),
                )
            )
        }
    }

    fun containsAnyIn(other: Iterable<E>) {
        val other = IterableSnapshot(other)
        if (other.list.isEmpty()) {
            return
        }

        if (actual.list.none { a -> other.list.any { e -> comparison.matches(a, e) } }) {
            report(
                Summaries.EXPECTED_COLLECTION_CONTAINS,
                DetailsFor.expectedActual("at least one element from", other.snapshot, actual.snapshot)
            )
        }
    }

    fun containsAllIn(other: Iterable<E>): OrderedAsserter {
        val other = IterableSnapshot(other)

        val matching = Matching(actual.list, other.list, comparison)
        val missing = matching.missing
        if (missing.isNotEmpty()) {
            report(
                Summaries.EXPECTED_COLLECTION_CONTAINS,
                DetailsFor.expectedActual(
                    "all elements from",
                    other.snapshot,
                    actual.snapshot,
                    expectedContext = other.list.anchorOnCopiesOf(missing.firstIndex),
                ).apply {
                    addLeftovers(DetailsFor.MISSING, DetailsFor.POSSIBLY_MISSING, missing, other.list)
                }
            )
            return skipInOrderCheck()
        }
        return OrderedAsserter(createInOrderCheck(other))
    }

    fun containsNoneIn(other: Iterable<E>) {
        val other = IterableSnapshot(other)

        val unexpectedIndices = actual.list.indices.filter { i ->
            other.list.any { e -> comparison.matches(actual.list[i], e) }
        }
        if (unexpectedIndices.isNotEmpty()) {
            val firstUnexpectedIndex = unexpectedIndices.first()
            val firstUnexpected = actual.list[firstUnexpectedIndex]
            report(
                Summaries.EXPECTED_COLLECTION_NOT_CONTAINS,
                DetailsFor.expectedActual(
                    "no elements from",
                    other.snapshot,
                    actual.snapshot,
                    expectedContext = other.list
                        .indexOfFirst { comparison.matches(firstUnexpected, it) }
                        .toCenteredOnContext(),
                    actualContext = firstUnexpectedIndex.toCenteredOnContext(),
                ).apply {
                    // Each unexpected element is listed once, however many copies there are, like `IterableSubject`
                    val unexpected = unexpectedIndices.map { actual.list[it] }.distinct()
                    add(DetailsFor.UNEXPECTED to unexpected, KeepStartContext)
                }
            )
        }
    }

    fun containsExactly(other: Iterable<E>): OrderedAsserter {
        val other = IterableSnapshot(other)

        val matching = Matching(actual.list, other.list, comparison)
        val missing = matching.missing
        val extraneous = matching.extraneous
        if (missing.isNotEmpty() || extraneous.isNotEmpty()) {
            report(
                Summaries.EXPECTED_COLLECTION_CONTAINS_EXACTLY,
                DetailsFor.expectedActual(
                    "exactly all elements from",
                    other.snapshot,
                    actual.snapshot,
                    expectedContext = other.list.anchorOnCopiesOf(missing.firstIndex),
                    actualContext = actual.list.anchorOnCopiesOf(extraneous.firstIndex),
                ).apply {
                    addLeftovers(DetailsFor.MISSING, DetailsFor.POSSIBLY_MISSING, missing, other.list)
                    addLeftovers(DetailsFor.EXTRANEOUS, DetailsFor.POSSIBLY_EXTRANEOUS, extraneous, actual.list)
                }
            )
            return skipInOrderCheck()
        }

        return OrderedAsserter(createInOrderCheck(other))
    }

    /**
     * Creates a check that [expected]'s elements appear in [actual] in the same order, though not necessarily next to
     * each other.
     */
    private fun createInOrderCheck(expected: IterableSnapshot<E>): () -> Unit = {
        // It's possible that our lists might have duplicate values - make sure we don't ever recount the same element
        // in order!
        val usedIndices = mutableSetOf<Int>()
        var actualIndex = 0
        var expectedIndex = 0
        var lastMatchIndex: Int? = null

        while (expectedIndex < expected.list.size) {
            val lookingFor = expected.list[expectedIndex]
            while (actualIndex < actual.list.size) {
                if (!usedIndices.contains(actualIndex) && comparison.matches(actual.list[actualIndex], lookingFor)) {
                    usedIndices.add(actualIndex)
                    lastMatchIndex = actualIndex
                    break
                }
                ++actualIndex
            }
            if (actualIndex == actual.list.size) {
                // If we got here, we couldn't find the next element from [expected]
                // Where the element sits in the actual list, which is before where the elements so far were found
                val foundIndex = actual.list.indices.firstOrNull {
                    !usedIndices.contains(it) && comparison.matches(actual.list[it], lookingFor)
                }
                // Show the element next to the previous match, which it was expected to come after
                val actualContext = if (foundIndex != null && lastMatchIndex != null) {
                    StringifierContext(
                        Truncation.Anchored(Anchor.CenteredOn(foundIndex), Anchor.CenteredOn(lastMatchIndex))
                    )
                } else {
                    foundIndex.toCenteredOnContext()
                }
                report(
                    Summaries.EXPECTED_COLLECTION_ORDERED,
                    DetailsFor.expectedActual(
                        "in order",
                        expected.snapshot,
                        actual.snapshot,
                        expectedContext = expectedIndex.toCenteredOnContext(),
                        actualContext = actualContext,
                    ).apply {
                        add(DetailsFor.OUT_OF_ORDER to lookingFor)
                    }
                )
                break
            }

            ++expectedIndex
        }

        // If we got here -- congrats! All elements of [expected] were found in [actual] in order.
    }

    private fun report(summary: String, details: Details) {
        interceptDetails(details)
        parent.report(Report(summary, details))
    }
}

/**
 * Adds the [leftovers] of [list]: the certain ones under [label], and each choice, e.g. "1 of [ "a", "ab" ]", under
 * [possiblyLabel].
 */
private fun <T> Details.addLeftovers(label: String, possiblyLabel: String, leftovers: Leftovers, list: List<T>) {
    if (leftovers.certainIndices.isNotEmpty()) {
        add(label to leftovers.certainIndices.map { list[it] }, KeepStartContext)
    }
    if (leftovers.choices.isNotEmpty()) {
        val choices = leftovers.choices.joinToString(", ") { choice ->
            val elements = choice.indices.map { list[it] }
            "${choice.count} of ${ValueStringifier.createFor(elements, KeepStartContext).render()}"
        }
        add(possiblyLabel to AnyStringifier(choices))
    }
}

/**
 * We don't want to test inorder if a check already failed, so provide this [OrderedAsserter]\
 * instead, which is guaranteed to pass.
 */
private fun skipInOrderCheck() = OrderedAsserter {}

/**
 * The elements of one list that a closest pairing leaves out, i.e. what's really missing or extraneous.
 *
 * When elements can't all be paired up, there can be several equally close pairings, which leave out different
 * elements. So only some left-out elements are certain; for the rest, which ones are left out is a free choice.
 *
 * @property certainIndices The left-out elements that every closest pairing leaves out too (or equal copies of them).
 * @property choices Groups of elements from which every closest pairing leaves out the same number, but not
 *   necessarily the same ones.
 */
private class Leftovers(val certainIndices: List<Int>, val choices: List<Choice>) {
    class Choice(val count: Int, val indices: List<Int>)

    fun isNotEmpty() = certainIndices.isNotEmpty() || choices.isNotEmpty()

    /** The first left-out element, e.g. to center a long list on, or `null` if there's none. */
    val firstIndex: Int? = certainIndices.firstOrNull() ?: choices.firstOrNull()?.indices?.first()
}

/**
 * Pairs up [actual] and [expected] elements that match, as many pairs as possible, using each element at most once.
 * Whatever is left unpaired is what's really [missing] or [extraneous].
 *
 * With [ElementComparison.Equality], pairing each expected element with the first free actual element it equals is
 * enough. Any element it equals, the others equal too, so moving them around can't make more pairs, or change which
 * values are left out.
 *
 * Other comparisons need a real search. For example, when matching means "contains", `actual = ["ab", "a"]` and
 * `expected = ["a", "ab"]` can be paired up, but greedily pairing expected "a" with the first actual element that
 * contains it, "ab", leaves expected "ab" with only actual "a".
 */
private class Matching<A, E>(
    private val actual: List<A>,
    private val expected: List<E>,
    private val comparison: ElementComparison<A, E>,
) {
    // The expected index each actual element is paired with, or -1
    private val pairedExpected = IntArray(actual.size) { -1 }

    val missing: Leftovers
    val extraneous: Leftovers

    init {
        if (comparison is ElementComparison.Equality) {
            missing = Leftovers(pairGreedily(), choices = emptyList())
            extraneous = Leftovers(actual.indices.filter { pairedExpected[it] == -1 }, choices = emptyList())
        } else {
            val candidates = expected.map { e -> actual.indices.filter { comparison.matches(actual[it], e) } }
            val unmatchedExpected = pairBySearching(candidates)

            val pairedActual = IntArray(expected.size) { -1 }
            actual.indices.filter { pairedExpected[it] != -1 }.forEach { pairedActual[pairedExpected[it]] = it }
            val actualCandidates = List(actual.size) { mutableListOf<Int>() }
            candidates.forEachIndexed { e, matches -> matches.forEach { actualCandidates[it].add(e) } }

            missing = findLeftovers(expected, unmatchedExpected, { candidates[it] }, { pairedExpected[it] })
            extraneous = findLeftovers(
                actual,
                actual.indices.filter { pairedExpected[it] == -1 },
                { actualCandidates[it] },
                { pairedActual[it] },
            )
        }
    }

    /**
     * Sorts the [unmatched] indices of [elements] into certain ones and choices.
     *
     * A different closest pairing could leave out another element instead of an unmatched one: one paired with
     * something the unmatched element matches (which would take its partner), or one paired with something *that*
     * element matches, and so on. Unmatched elements whose alternatives overlap form one group. If everything in a
     * group is equal, the left-out values are certain; otherwise, which ones are left out is a choice.
     *
     * @param matchesOf The indices in the other list that an element matches.
     * @param partnerOf The index of the element in this list paired with an element of the other list.
     */
    private fun <T> findLeftovers(
        elements: List<T>,
        unmatched: List<Int>,
        matchesOf: (Int) -> List<Int>,
        partnerOf: (Int) -> Int,
    ): Leftovers {
        // Each element's group, tracked by pointing it at another element of the same group until reaching the group's
        // representative, which points at itself
        val groupParent = IntArray(elements.size) { it }
        fun findGroup(i: Int): Int {
            var group = i
            while (groupParent[group] != group) group = groupParent[group]
            return group
        }

        val reached = BooleanArray(elements.size)
        unmatched.forEach { reached[it] = true }
        val toVisit = ArrayDeque(unmatched)
        while (toVisit.isNotEmpty()) {
            val i = toVisit.removeFirst()
            for (match in matchesOf(i)) {
                // Every match is paired, or the two could be paired too, and the pairing wouldn't be a closest one
                val alternative = partnerOf(match)
                groupParent[findGroup(alternative)] = findGroup(i)
                if (!reached[alternative]) {
                    reached[alternative] = true
                    toVisit.addLast(alternative)
                }
            }
        }

        val isUnmatched = BooleanArray(elements.size)
        unmatched.forEach { isUnmatched[it] = true }
        val certainIndices = mutableListOf<Int>()
        val choices = mutableListOf<Leftovers.Choice>()
        for (group in elements.indices.filter { reached[it] }.groupBy { findGroup(it) }.values) {
            val leftOut = group.filter { isUnmatched[it] }
            if (group.all { elements[it] == elements[group.first()] }) {
                certainIndices += leftOut
            } else {
                choices += Leftovers.Choice(leftOut.size, group)
            }
        }
        return Leftovers(certainIndices.sorted(), choices)
    }

    // Returns the expected indices left unpaired
    private fun pairGreedily(): List<Int> = expected.indices.filter { e ->
        val a = actual.indices.firstOrNull { pairedExpected[it] == -1 && comparison.matches(actual[it], expected[e]) }
        if (a != null) pairedExpected[a] = e
        a == null
    }

    // Returns the expected indices left unpaired. `candidates` lists the actual indices each expected element matches.
    private fun pairBySearching(candidates: List<List<Int>>): List<Int> {
        // Pairs expected element `start` with an actual one. If every actual element it matches is taken, frees one up
        // by moving that element's partner to another actual element the partner matches, which may bump another pair
        // in turn, until some element in the chain lands on a free actual element. This is a standard way to solve
        // "bipartite matching". If no chain works, `start` stays unpaired. The chain is tracked in a list instead of by
        // recursing, to avoid long collections overflowing the call stack.
        fun tryPair(start: Int): Boolean {
            // Pairing with a free candidate right away, when there is one, avoids most searches
            candidates[start].firstOrNull { pairedExpected[it] == -1 }?.let { a ->
                pairedExpected[a] = start
                return true
            }

            val visited = BooleanArray(actual.size)
            // Each step of the chain: an expected element, how many of its candidates were tried, and the actual
            // element it's currently paired with, which the previous step wants to take from it (-1 for `start`)
            val chain = mutableListOf(intArrayOf(start, 0, -1))
            while (chain.isNotEmpty()) {
                val step = chain.last()
                val e = step[0]
                if (step[1] == candidates[e].size) {
                    chain.removeAt(chain.lastIndex)
                    continue
                }
                val a = candidates[e][step[1]++]
                if (visited[a]) continue
                visited[a] = true

                val owner = pairedExpected[a]
                if (owner != -1) {
                    chain.add(intArrayOf(owner, 0, a))
                    continue
                }

                // `a` is free, so each step takes the actual element of the step after it, and the last step takes `a`
                pairedExpected[a] = e
                for (i in chain.lastIndex downTo 1) {
                    pairedExpected[chain[i][2]] = chain[i - 1][0]
                }
                return true
            }
            return false
        }

        return expected.indices.filter { e -> !tryPair(e) }
    }
}
