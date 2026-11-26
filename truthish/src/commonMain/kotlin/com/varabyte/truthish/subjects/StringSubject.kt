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
    /**
     * Like [AnySubject.isEqualTo], except two strings that span multiple lines are shown as a line-by-line diff.
     *
     * @param ignoreCase If true, characters that differ only in case are considered equal.
     */
    fun isEqualTo(expected: CharSequence?, ignoreCase: Boolean = false) {
        if (expected is String && actual.equals(expected, ignoreCase)) return
        val details = expected
            ?.let {
                createLineDiffDetails(it.toString(), actual, ignoreCase)
                    ?: createStringEqualityDetails(it, actual, ignoreCase)
            }
            ?: DetailsFor.expectedActual(null, actual)
        report(Report(Summaries.EXPECTED_EQUAL, details))
    }

    /**
     * Like [AnySubject.isNotEqualTo], with an option to ignore case.
     *
     * @param ignoreCase If true, characters that differ only in case are considered equal.
     */
    fun isNotEqualTo(expected: CharSequence?, ignoreCase: Boolean = false) {
        if (!(expected is String && actual.equals(expected, ignoreCase))) return
        val details = if (ignoreCase) {
            // The two values may differ in case, so show both
            DetailsFor.expectedActual("not to equal", expected, actual).apply { addNote(CASE_IGNORED_NOTE) }
        } else {
            DetailsFor.actual(actual)
        }
        report(Report(Summaries.EXPECTED_NOT_EQUAL, details))
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
            // The strings can differ anywhere in a long prefix, so show both around where they do
            val context = expected.toList().createSharedContext(actual.toList(), Truncators.forStrings)
            report(
                Report(
                    Summaries.EXPECTED_STARTS_WITH,
                    DetailsFor.expectedActual(
                        "to start with",
                        expected,
                        actual,
                        expectedContext = context,
                        actualContext = context,
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
                        expectedContext = KeepStartContext,
                        actualContext = KeepStartContext,
                    )
                )
            )
        }
    }

    fun endsWith(expected: CharSequence) {
        if (!actual.endsWith(expected)) {
            // The strings can differ anywhere in a long suffix, so show both around where they do: the last character
            // before the end they share. All of `actual` may be shared (e.g. "c" doesn't end with "abc"), so clamp it.
            val sharedEndLength = actual.commonSuffixWith(expected).length
            val expectedDiffIndex = expected.length - 1 - sharedEndLength
            val actualDiffIndex = (actual.length - 1 - sharedEndLength).coerceAtLeast(0)
            report(
                Report(
                    Summaries.EXPECTED_ENDS_WITH,
                    DetailsFor.expectedActual(
                        "to end with",
                        expected,
                        actual,
                        expectedContext = expectedDiffIndex.toCenteredOnContext(),
                        actualContext = actualDiffIndex.toCenteredOnContext(),
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
                        expectedContext = expected.toKeepEndContext(),
                        actualContext = actual.toKeepEndContext(),
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
                        actualContext = (foundIndex until foundIndex + expected.length).toCenteredOnContext(),
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
                        actualContext = match.range.toCenteredOnContext(),
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
private fun createStringEqualityDetails(expected: CharSequence, actual: String, ignoreCase: Boolean): Details {
    val context = expected.toComparableChars(ignoreCase)
        .createSharedContext(actual.toComparableChars(ignoreCase), Truncators.forStrings)
    return DetailsFor.expectedActual(expected, actual, expectedContext = context, actualContext = context)
        .apply { if (ignoreCase) addNote(CASE_IGNORED_NOTE) }
}

/** This string's characters, for finding where two strings first differ. */
private fun CharSequence.toComparableChars(ignoreCase: Boolean): List<Char> =
    // "uppercase -> lowercase" is intentional, and is different from just doing "lowercase" for some characters. This
    // is what `Char.equals` does under the hood.
    if (ignoreCase) map { it.uppercaseChar().lowercaseChar() } else toList()

private const val CASE_IGNORED_NOTE = "case was ignored"

/**
 * Details showing how two strings differ line by line, or `null` if a line diff wouldn't help, e.g. because neither
 * string spans multiple lines.
 */
private fun createLineDiffDetails(expectedText: String, actualText: String, ignoreCase: Boolean): Details? {
    if (!expectedText.contains('\n') && !actualText.contains('\n')) return null

    // If only line endings differ, a diff would look empty. Instead, show the values as single-line reports, whose
    // escaped `\r`s make the difference easier to see. For example:
    // Expected: "a\nb\nc"
    // But was : "a\r\nb\r\nc"
    if (expectedText.replace("\r\n", "\n").equals(actualText.replace("\r\n", "\n"), ignoreCase)) return null

    // Strings too big to diff also show the single-line reports, truncated, leaving the full comparison to the IDE's
    // diff viewer, where available
    val diff = LineDiff.tryCreate(expectedText, actualText, ignoreCase) ?: return null

    return Details(listOf(DetailsFor.DIFF to diff)).apply {
        if (ignoreCase) addNote(CASE_IGNORED_NOTE)
        // The diff ignores line endings, which would otherwise mark every line as changed, so call them out separately
        val expectedEndings = expectedText.findLineEndings()
        val actualEndings = actualText.findLineEndings()
        if (expectedEndings.isNotEmpty() && actualEndings.isNotEmpty() && expectedEndings != actualEndings) {
            addNote("line endings also differ")
        }
        if (expectedText.endsWith('\n') && !actualText.endsWith('\n')) {
            addNote("expected ends with a newline but actual doesn't")
        }
        if (!expectedText.endsWith('\n') && actualText.endsWith('\n')) {
            addNote("actual ends with a newline but expected doesn't")
        }
    }
}

/** The distinct line endings (`\n` and `\r\n`) used in this string. */
private fun String.findLineEndings(): Set<String> = Regex("\r?\n").findAll(this).map { it.value }.toSet()

private fun CharSequence.toKeepEndContext() = StringifierContext(Truncation.Anchored(Anchor.CenteredOn(lastIndex)))

// Centers a long string on the middle of this range, e.g. a match, so a long match isn't cut off at one end
private fun IntRange.toCenteredOnContext() =
    StringifierContext(Truncation.Anchored(Anchor.CenteredOn(first + (last - first) / 2)))
