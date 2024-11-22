package com.varabyte.truthish.failure

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

internal fun ReportError.assertSubstrings(vararg substrings: String) {
    this.report.assertSubstrings(*substrings)
}
