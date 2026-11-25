package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.AnyStringifier
import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.ValueStringifier

/**
 * A subject that supports comparing one value to another
 */
open class ComparableSubject<T: Comparable<T>>(private val actual: T): NotNullSubject<T>(actual) {
    fun isGreaterThan(target: T) {
        if (actual <= target) {
            report(Report(Summaries.EXPECTED_COMPARISON, DetailsFor.expectedActual("greater than", target, actual)))
        }
    }

    fun isGreaterThanEqual(target: T) {
        if (actual < target) {
            report(Report(Summaries.EXPECTED_COMPARISON, DetailsFor.expectedActual("greater than or equal to", target, actual)))
        }
    }

    fun isLessThanEqual(target: T) {
        if (actual > target) {
            report(Report(Summaries.EXPECTED_COMPARISON, DetailsFor.expectedActual("less than or equal to", target, actual)))
        }
    }

    fun isLessThan(target: T) {
        if (actual >= target) {
            report(Report(Summaries.EXPECTED_COMPARISON, DetailsFor.expectedActual("less than", target, actual)))
        }
    }

    fun isIn(range: ClosedRange<T>) {
        if (actual !in range) {
            report(Report(Summaries.EXPECTED_IN_RANGE, DetailsFor.expectedActual("in", range.toBoundsString(), actual)))
        }
    }

    fun isNotIn(range: ClosedRange<T>) {
        if (actual in range) {
            report(
                Report(
                    Summaries.EXPECTED_NOT_IN_RANGE,
                    DetailsFor.expectedActual("not in", range.toBoundsString(), actual),
                )
            )
        }
    }
}

/**
 * Renders a range by its bounds, e.g. `1..10`, since a range that's also a collection (e.g. an `IntRange`) would
 * otherwise render as all of its elements.
 */
private fun ClosedRange<*>.toBoundsString(): AnyStringifier {
    val start = ValueStringifier.createFor(start).render()
    val end = ValueStringifier.createFor(endInclusive).render()
    return AnyStringifier("$start..$end")
}