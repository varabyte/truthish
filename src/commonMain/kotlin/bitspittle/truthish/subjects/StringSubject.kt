package bitspittle.truthish.subjects

import bitspittle.truthish.failure.DetailsFor
import bitspittle.truthish.failure.Report
import bitspittle.truthish.failure.Summaries

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
            report(Report(Summaries.EXPECTED_NOT_BLANK))
        }
    }

    fun hasLength(expectedLength: Int) {
        if (actual.length != expectedLength) {
            report(
                Report(
                    Summaries.EXPECTED_COMPARISON,
                    DetailsFor.expectedActual("length", expectedLength, actual.length)
                )
            )
        }
    }

    fun startsWith(expected: CharSequence) {
        if (!actual.startsWith(expected)) {
            report(
                Report(Summaries.EXPECTED_STARTS_WITH, DetailsFor.expectedActual("starts with", expected, actual))
            )
        }
    }

    fun doesNotStartWith(expected: CharSequence) {
        if (actual.startsWith(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_NOT_STARTS_WITH,
                    DetailsFor.expectedActual("not to start with", expected, actual)
                )
            )
        }
    }

    fun endsWith(expected: CharSequence) {
        if (!actual.endsWith(expected)) {
            report(
                Report(Summaries.EXPECTED_ENDS_WITH, DetailsFor.expectedActual("ends with", expected, actual))
            )
        }
    }

    fun doesNotEndWith(expected: CharSequence) {
        if (actual.endsWith(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_NOT_ENDS_WITH,
                    DetailsFor.expectedActual("not to end with", expected, actual)
                )
            )
        }
    }

    fun matches(regex: Regex) {
        if (!regex.matches(actual)) {
            report(
                Report(Summaries.EXPECTED_MATCH, DetailsFor.expectedActual("to match", regex.pattern, actual))
            )
        }
    }

    fun matches(regex: String) {
        matches(Regex(regex))
    }

    fun doesNotMatch(regex: Regex) {
        if (regex.matches(actual)) {
            report(
                Report(Summaries.EXPECTED_NOT_MATCH, DetailsFor.expectedActual("not to match", regex.pattern, actual))
            )
        }
    }

    fun doesNotMatch(regex: String) {
        doesNotMatch(Regex(regex))
    }

    fun contains(expected: CharSequence) {
        if (!actual.contains(expected)) {
            report(
                Report(Summaries.EXPECTED_CONTAINS, DetailsFor.expectedActual("to contain", expected, actual))
            )
        }
    }

    fun containsMatch(regex: Regex) {
        if (!actual.contains(regex)) {
            report(
                Report(
                    Summaries.EXPECTED_CONTAINS,
                    DetailsFor.expectedActual("to contain match", regex.pattern, actual)
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
                Report(Summaries.EXPECTED_NOT_CONTAINS, DetailsFor.expectedActual("to not contain", expected, actual))
            )
        }
    }

    fun doesNotContainMatch(regex: Regex) {
        if (actual.contains(regex)) {
            report(
                Report(
                    Summaries.EXPECTED_NOT_CONTAINS,
                    DetailsFor.expectedActual("to not contain match", regex.pattern, actual)
                )
            )
        }
    }

    fun doesNotContainMatch(regex: String) {
        doesNotContainMatch(Regex(regex))
    }
}
