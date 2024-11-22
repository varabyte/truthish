package com.varabyte.truthish.failure

/**
 * Verify that all of the passed in substrings are present in the final report.
 *
 * It can be difficult to test the exact output of a failure report, and it would be fragile to check the content of the
 * whole string, since text can change over time. However, checking for the presence of certain substrings can be a
 * reasonable compromise.
 */
fun Report.assertSubstrings(vararg substrings: String) {
    val reportStr = this.toString()
    for (substring in substrings) {
        if (!reportStr.contains(substring)) {
            throw AssertionError(
                """
The failure did not contain "$substring"

Original report:

$reportStr
                """
            )
        }
    }
}

/**
 * @see [Report.assertSubstrings]
 */
internal fun ReportError.assertSubstrings(vararg substrings: String) {
    this.report.assertSubstrings(*substrings)
}
