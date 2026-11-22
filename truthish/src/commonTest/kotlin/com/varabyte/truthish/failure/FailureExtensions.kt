package com.varabyte.truthish.failure

private fun Char.isWordChar() = isLetterOrDigit() || this == '_'

// Finds `substring` at or after `startIndex`, but only where it doesn't start or end in the middle of a word, so e.g.
// "Int" doesn't match inside "IntValue"
private fun String.indexOfWhole(substring: String, startIndex: Int): Int {
    var index = indexOf(substring, startIndex)
    while (index >= 0) {
        val end = index + substring.length
        val splitsWordBefore = substring.first().isWordChar() && index > 0 && this[index - 1].isWordChar()
        val splitsWordAfter = substring.last().isWordChar() && end < length && this[end].isWordChar()
        if (!splitsWordBefore && !splitsWordAfter) return index
        index = indexOf(substring, index + 1)
    }
    return -1
}

private fun String.assertContains(substrings: Array<out String>) {
    val reportStr = this
    var searchFrom = 0
    for (substring in substrings) {
        val index = reportStr.indexOfWhole(substring, searchFrom)
        if (index < 0) {
            val problem = when {
                searchFrom > 0 && reportStr.indexOfWhole(substring, 0) >= 0 ->
                    "contained \"$substring\", but not after the substrings before it"
                reportStr.contains(substring) -> "contained \"$substring\", but only as part of a longer word"
                else -> "did not contain \"$substring\""
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
 * Verify that all of the passed in substrings are present in the final report, in order, as whole words.
 *
 * In other words, searching for the substring "Int" will not match "Integer".
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
