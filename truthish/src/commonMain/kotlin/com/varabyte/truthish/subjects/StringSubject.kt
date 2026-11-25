package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.Details
import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.StringifierContext
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.Truncators
import com.varabyte.truthish.util.diff.LineDiff
import com.varabyte.truthish.util.truncate.Anchor
import com.varabyte.truthish.util.truncate.Truncation

class StringSubject(private val actual: String) : ComparableSubject<String>(actual) {
    /** Like [AnySubject.isEqualTo], except two strings that span multiple lines are shown as a line-by-line diff. */
    fun isEqualTo(expected: CharSequence?) {
        if (actual == expected) return
        val details = expected
            ?.let { createLineDiffDetails(it.toString(), actual) ?: createStringEqualityDetails(it, actual) }
            ?: DetailsFor.expectedActual(null, actual)
        report(Report(Summaries.EXPECTED_EQUAL, details))
    }

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

/** Details for two unequal strings, both truncated around where they first differ. */
private fun createStringEqualityDetails(expected: CharSequence, actual: String): Details {
    val context = expected.toList().createSharedContext(actual.toList(), Truncators.forStrings)
    return DetailsFor.expectedActual(expected, actual, expectedContext = context, actualContext = context)
}

/**
 * Details showing how two strings differ line by line, or `null` if a line diff wouldn't help, e.g. because neither
 * string spans multiple lines.
 */
private fun createLineDiffDetails(expectedText: String, actualText: String): Details? {
    if (!expectedText.contains('\n') && !actualText.contains('\n')) return null

    // If only line endings differ, a diff would look empty. Instead, show the values as single-line reports, whose
    // escaped `\r`s make the difference easier to see. For example:
    // Expected: "a\nb\nc"
    // But was : "a\r\nb\r\nc"
    if (expectedText.replace("\r\n", "\n") == actualText.replace("\r\n", "\n")) return null

    // Strings too big to diff also show the single-line reports, truncated, leaving the full comparison to the IDE's
    // diff viewer, where available
    val diff = LineDiff.tryCreate(expectedText, actualText) ?: return null

    // The diff ignores line endings, which would otherwise mark every line as changed, so call them out separately
    val notes = buildList {
        val expectedEndings = expectedText.findLineEndings()
        val actualEndings = actualText.findLineEndings()
        if (expectedEndings.isNotEmpty() && actualEndings.isNotEmpty() && expectedEndings != actualEndings) {
            add("line endings also differ")
        }
        if (expectedText.endsWith('\n') && !actualText.endsWith('\n')) {
            add("expected ends with a newline but actual doesn't")
        }
        if (!expectedText.endsWith('\n') && actualText.endsWith('\n')) {
            add("actual ends with a newline but expected doesn't")
        }
    }
    val extras = if (notes.isNotEmpty()) mapOf(DetailsFor.DIFF to notes.joinToString("; ")) else emptyMap()
    return Details(listOf(DetailsFor.DIFF to diff), extras)
}

/** The distinct line endings (`\n` and `\r\n`) used in this string. */
private fun String.findLineEndings(): Set<String> = Regex("\r?\n").findAll(this).map { it.value }.toSet()

// Only the start matters to `startsWith` and its opposite, so a long string gives it the whole window
private val StartContext = StringifierContext(Truncation.Anchored(Anchor.CenteredOn(0)))

// Only the end matters to `endsWith` and its opposite, so a long string gives it the whole window
private fun String.toEndContext() = StringifierContext(Truncation.Anchored(Anchor.CenteredOn(lastIndex)))

// Centers a long string on the middle of this range, e.g. a match, so a long match isn't cut off at one end
private fun IntRange.toCenteredContext() =
    StringifierContext(Truncation.Anchored(Anchor.CenteredOn(first + (last - first) / 2)))
