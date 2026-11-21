package com.varabyte.truthish.failure

private fun String.assertContains(substrings: Array<out String>) {
    val reportStr = this
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
 * Verify that all of the passed in substrings are present in the final report.
 *
 * It can be difficult to test the exact output of a failure report, and it would be fragile to check the content of the
 * whole string, since text can change over time. However, checking for the presence of certain substrings can be a
 * reasonable compromise.
 */
fun Report.assertSubstrings(vararg substrings: String) {
    this.toString().assertContains(substrings)
}

/**
 * @see [Report.assertSubstrings]
 */
internal fun ReportError.assertSubstrings(vararg substrings: String) {
    this.report.assertSubstrings(*substrings)
}

/**
 * @see [Report.assertSubstrings]
 */
internal fun AssertionError.assertSubstrings(vararg substrings: String) {
    message.orEmpty().assertContains(substrings)
}
