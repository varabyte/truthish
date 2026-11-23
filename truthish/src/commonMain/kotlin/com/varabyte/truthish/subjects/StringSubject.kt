package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Summaries

class StringSubject(private val actual: String) : ComparableSubject<String>(actual) {

    fun isEmpty() {
        if (!actual.isEmpty()) {
            report(Report(Summaries.EXPECTED_EMPTY, DetailsFor.actual(actual)))
        }
    }

    fun isNotEmpty() {
        if (actual.isEmpty()) {
            report(Report(Summaries.EXPECTED_NOT_EMPTY))
        }
    }

    fun isBlank() {
        if (!actual.isBlank()) {
            report(Report(Summaries.EXPECTED_BLANK, DetailsFor.actual(actual)))
        }
    }

    fun isNotBlank() {
        if (actual.isBlank()) {
            report(Report(Summaries.EXPECTED_NOT_BLANK, DetailsFor.actual(actual)))
        }
    }

    fun hasLength(expectedLength: Int) {
        if (actual.length != expectedLength) {
            report(
                Report(
                    Summaries.EXPECTED_LENGTH,
                    DetailsFor.expectedActual(expectedLength, actual.length).apply {
                        add(DetailsFor.VALUE to actual)
                    }
                )
            )
        }
    }

    fun startsWith(expected: CharSequence) {
        if (!actual.startsWith(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_STARTS_WITH,
                    DetailsFor.expectedActual(
                        "to start with",
                        expected,
                        actual,
                        expectedContext = FailureValueContext,
                    )
                )
            )
        }
    }

    fun doesNotStartWith(expected: CharSequence) {
        if (actual.startsWith(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_NOT_STARTS_WITH,
                    DetailsFor.expectedActual(
                        "not to start with",
                        expected,
                        actual,
                        expectedContext = FailureValueContext,
                    )
                )
            )
        }
    }

    fun endsWith(expected: CharSequence) {
        if (!actual.endsWith(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_ENDS_WITH,
                    DetailsFor.expectedActual(
                        "to end with",
                        expected,
                        actual,
                        expectedContext = FailureValueContext,
                    )
                )
            )
        }
    }

    fun doesNotEndWith(expected: CharSequence) {
        if (actual.endsWith(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_NOT_ENDS_WITH,
                    DetailsFor.expectedActual(
                        "not to end with",
                        expected,
                        actual,
                        expectedContext = FailureValueContext,
                    )
                )
            )
        }
    }

    fun matches(regex: Regex) {
        if (!regex.matches(actual)) {
            report(
                Report(
                    Summaries.EXPECTED_MATCH,
                    DetailsFor.expectedActual(
                        "to match",
                        regex.pattern,
                        actual,
                        expectedContext = FailureValueContext,
                    )
                )
            )
        }
    }

    fun matches(regex: String) {
        matches(Regex(regex))
    }

    fun doesNotMatch(regex: Regex) {
        if (regex.matches(actual)) {
            report(
                Report(
                    Summaries.EXPECTED_NOT_MATCH,
                    DetailsFor.expectedActual(
                        "not to match",
                        regex.pattern,
                        actual,
                        expectedContext = FailureValueContext,
                    )
                )
            )
        }
    }

    fun doesNotMatch(regex: String) {
        doesNotMatch(Regex(regex))
    }

    fun contains(expected: CharSequence) {
        if (!actual.contains(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_CONTAINS,
                    DetailsFor.expectedActual(
                        "to contain",
                        expected,
                        actual,
                        expectedContext = FailureValueContext,
                    )
                )
            )
        }
    }

    fun containsMatch(regex: Regex) {
        if (!actual.contains(regex)) {
            report(
                Report(
                    Summaries.EXPECTED_CONTAINS,
                    DetailsFor.expectedActual(
                        "to contain match",
                        regex.pattern,
                        actual,
                        expectedContext = FailureValueContext,
                    )
                )
            )
        }
    }

    fun containsMatch(regex: String) {
        containsMatch(Regex(regex))
    }

    fun doesNotContain(expected: CharSequence) {
        if (actual.contains(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_NOT_CONTAINS,
                    DetailsFor.expectedActual(
                        "not to contain",
                        expected,
                        actual,
                        expectedContext = FailureValueContext,
                    )
                )
            )
        }
    }

    fun doesNotContainMatch(regex: Regex) {
        if (actual.contains(regex)) {
            report(
                Report(
                    Summaries.EXPECTED_NOT_CONTAINS,
                    DetailsFor.expectedActual(
                        "not to contain match",
                        regex.pattern,
                        actual,
                        expectedContext = FailureValueContext,
                    )
                )
            )
        }
    }

    fun doesNotContainMatch(regex: String) {
        doesNotContainMatch(Regex(regex))
    }
}