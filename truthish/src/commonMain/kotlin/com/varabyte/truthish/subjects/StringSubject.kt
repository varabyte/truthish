package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.StringifierContext
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.util.truncate.Anchor
import com.varabyte.truthish.util.truncate.Truncation

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
                        actualContext = StartContext,
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
                        actualContext = StartContext,
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
                        actualContext = actual.toEndContext(),
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
                        actualContext = actual.toEndContext(),
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
        val foundIndex = actual.indexOf(expected.toString())
        if (foundIndex >= 0) {
            report(
                Report(
                    Summaries.EXPECTED_NOT_CONTAINS,
                    DetailsFor.expectedActual(
                        "not to contain",
                        expected,
                        actual,
                        expectedContext = FailureValueContext,
                        actualContext = (foundIndex until foundIndex + expected.length).toCenteredContext(),
                    )
                )
            )
        }
    }

    fun doesNotContainMatch(regex: Regex) {
        val match = regex.find(actual)
        if (match != null) {
            report(
                Report(
                    Summaries.EXPECTED_NOT_CONTAINS,
                    DetailsFor.expectedActual(
                        "not to contain match",
                        regex.pattern,
                        actual,
                        expectedContext = FailureValueContext,
                        actualContext = match.range.toCenteredContext(),
                    )
                )
            )
        }
    }

    fun doesNotContainMatch(regex: String) {
        doesNotContainMatch(Regex(regex))
    }
}

// Only the start matters to `startsWith` and its opposite, so a long string gives it the whole window
private val StartContext = StringifierContext(Truncation.Anchored(Anchor.CenteredOn(0)))

// Only the end matters to `endsWith` and its opposite, so a long string gives it the whole window
private fun String.toEndContext() = StringifierContext(Truncation.Anchored(Anchor.CenteredOn(lastIndex)))

// Centers a long string on the middle of this range, e.g. a match, so a long match isn't cut off at one end
private fun IntRange.toCenteredContext() =
    StringifierContext(Truncation.Anchored(Anchor.CenteredOn(first + (last - first) / 2)))
