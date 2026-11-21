package com.varabyte.truthish.failure

internal class ReportError(val report: Report) : AssertionError(report.toString())

// `cause` is overridden because common Kotlin (as of the stdlib version we target) has no `AssertionError` constructor
// that takes one.
@PublishedApi
internal class AssertionWithCauseError(message: String, override val cause: Throwable?) : AssertionError(message)

/**
 * Creates the error to throw for this failed report.
 *
 * A [ReportError] wrapping the report always works, but some platforms have better options. For example, some test
 * runners can show a diff for special exception types, using the values in [comparison].
 *
 * [comparison] is set when the report compares two values that were expected to be equal.
 */
internal expect fun Report.createAssertionError(comparison: ComparedValues?): AssertionError

internal class MultipleReportsError(val reports: List<Report>, val summary: String? = null) : AssertionError(reports.buildErrorMessage(summary)) {
    companion object {
        private fun List<Report>.buildErrorMessage(summary: String?): String {
            val self = this
            return buildString {
                append("Grouped assertions had ${self.size} failure(s)\n")
                if (summary != null) {
                    appendLine("Summary: $summary")
                }
                appendLine()
                self.forEachIndexed { i, report ->
                    append("Failure ${i + 1}:\n")
                    append(report.toString())
                    appendLine()
                }
            }
        }
    }

    init {
        check(reports.isNotEmpty())
    }
}
