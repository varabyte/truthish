package com.varabyte.truthish.failure

internal fun ReportError.assertSubstrings(vararg substrings: String) {
    val reportStr = report.toString()

    for (substring in substrings) {
        if (!reportStr.contains(substring)) {
            throw AssertionError(
                """
The last failure did not contain "$substring"

Original report:

$reportStr

                """
            )
        }
    }
}
