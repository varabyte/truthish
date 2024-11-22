package com.varabyte.truthish.failure

/**
 * A strategy for how an assertion failure should be handled.
 *
 * The most common strategy is to throw an exception, but a custom strategy can be registered with
 * a [Reportable] if needed.
 */
interface FailureStrategy {
    fun handle(report: Report)
}

internal class ReportError(val report: Report) : AssertionError(report.toString())

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

/**
 * A strategy that will cause the test to fail immediately.
 */
class AssertionStrategy : FailureStrategy {
    override fun handle(report: Report) {
        throw ReportError(report)
    }
}

/**
 * A strategy that collects reports and defers them from asserting until we request it.
 *
 * This is useful for cases where multiple assertions are being made in a group, and you want to collect all the
 * failures at the same time, instead of dying on the first one you run into.
 */
class DeferredStrategy(private val summary: String? = null) : FailureStrategy {
    private val reports = mutableListOf<Report>()

    override fun handle(report: Report) {
        reports.add(report)
        report.details.add(DetailsFor.AT to
                Throwable()
                    .stackTraceToString().split("\n")
                    .drop(1) // Drop "java.lang.Throwable" line
                    .asSequence()
                    .map { it.removePrefix("\tat ") }
                    .filterNot { it.startsWith("com.varabyte.truthish.failure") || it.startsWith("com.varabyte.truthish.subjects") }
                    .first()
        )
    }

    fun handleNow() {
        if (reports.isNotEmpty()) {
            throw MultipleReportsError(reports, summary)
        }
    }
}
