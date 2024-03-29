package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Summaries

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
}