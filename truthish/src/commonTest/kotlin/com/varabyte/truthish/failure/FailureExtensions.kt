package com.varabyte.truthish.failure

private fun String.assertContains(substrings: Array<out String>) {
    val reportStr = this
    var searchFrom = 0
    for (substring in substrings) {
        val index = reportStr.indexOf(substring, searchFrom)
        if (index < 0) {
            val problem = if (searchFrom > 0 && reportStr.contains(substring)) {
                "contained \"$substring\", but not after the substrings before it"
            } else {
                "did not contain \"$substring\""
            }
            throw AssertionError(
                """
The failure $problem

Original report:

$reportStr
                """
            )
        }
        searchFrom = index + substring.length
    }
}

/**
 * Verify that all of the passed in substrings are present in the final report, in order.
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
internal fun AssertionError.assertSubstrings(vararg substrings: String) {
    message.orEmpty().assertContains(substrings)
}
