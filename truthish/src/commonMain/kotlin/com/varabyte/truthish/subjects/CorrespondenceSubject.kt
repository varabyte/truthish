package com.varabyte.truthish.subjects

import com.varabyte.truthish.Correspondence
import com.varabyte.truthish.failure.AnyStringifier
import com.varabyte.truthish.failure.Details
import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Reportable

/**
 * Assertions about a collection whose elements are compared with expected ones using a [Correspondence].
 *
 * Failures are reported through the collection's subject, so any message, name and failure
 * strategy (e.g. inside `assertAll`) applied to the parent still apply here.
 */
class CorrespondenceSubject<A, E> internal constructor(
    parent: Reportable,
    actual: IterableSnapshot<A>,
    correspondence: Correspondence<A, E>,
) {
    private val asserter = ContainmentAsserter(
        parent,
        actual,
        ElementComparison.Using(correspondence::compare),
        interceptDetails = { it.addTestingWhether(correspondence) },
    )

    fun contains(expected: E) = asserter.contains(expected)

    fun doesNotContain(expected: E) = asserter.doesNotContain(expected)

    fun containsAnyIn(other: Iterable<E>) = asserter.containsAnyIn(other)

    fun containsAnyIn(vararg elements: E) = containsAnyIn(elements.asIterable())

    fun containsAllIn(other: Iterable<E>): ConsecutiveOrderedAsserter = asserter.containsAllIn(other)

    fun containsAllIn(vararg elements: E) = containsAllIn(elements.asIterable())

    fun containsNoneIn(other: Iterable<E>) = asserter.containsNoneIn(other)

    fun containsNoneIn(vararg elements: E) = containsNoneIn(elements.asIterable())

    fun containsExactly(other: Iterable<E>): OrderedAsserter = asserter.containsExactly(other)

    fun containsExactly(vararg elements: E) = containsExactly(elements.asIterable())
}

fun <A, E> CorrespondenceSubject<A, E>.containsAnyIn(other: Array<out E>) = containsAnyIn(*other)
fun <A, E> CorrespondenceSubject<A, E>.containsAllIn(other: Array<out E>) = containsAllIn(*other)
fun <A, E> CorrespondenceSubject<A, E>.containsNoneIn(other: Array<out E>) = containsNoneIn(*other)
fun <A, E> CorrespondenceSubject<A, E>.containsExactly(other: Array<out E>) = containsExactly(*other)

/**
 * Adds a detail saying how elements were compared, e.g. "actual value contains expected value", after "But was".
 */
private fun Details.addTestingWhether(correspondence: Correspondence<*, *>) {
    val butWasIndex = items.indexOfFirst { it.first == DetailsFor.BUT_WAS }
    add(
        butWasIndex + 1,
        DetailsFor.TESTING_WHETHER to AnyStringifier("actual value ${correspondence.description} expected value"),
    )
}
