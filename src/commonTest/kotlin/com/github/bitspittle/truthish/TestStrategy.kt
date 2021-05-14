package com.github.bitspittle.truthish

import com.github.bitspittle.truthish.failure.FailureStrategy
import com.github.bitspittle.truthish.failure.Report

/**
 * A "reporter" that saves the last failure that was reported but does not abort.
 *
 * This is useful for testing.
 */
class TestStrategy : FailureStrategy {
    var lastReport: String? = null

    override fun handle(report: Report) {
        lastReport = report.toString().also { println("-------\n$it") }
    }

    /**
     * Verify that a failure report was received, optionally specifying substrings to search
     * against to ensure that we got a meaningful report.
     *
     * After this method is called, the last report is cleared, to that this [TestStrategy] can be
     * reused.
     */
    fun verifyFailureAndClear(vararg substrings: String) {
        if (lastReport == null) {
            throw AssertionError("A failure should have occurred but it did not")
        }
        val lastReportCopy = lastReport!!
        lastReport = null

        for (substring in substrings) {
            if (!lastReportCopy.contains(substring)) {
                throw AssertionError("""The last failure did not contain "$substring"

Full report:
${"=".repeat(80)}
$lastReportCopy
${"=".repeat(80)}
"""
                )
            }
        }
    }
}