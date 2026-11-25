package com.varabyte.truthish.util.diff

/**
 * A line-by-line diff between two multi-line strings, e.g. for showing where they differ in a report.
 *
 * Lines are split on both `\n` and `\r\n`, so two strings that only differ in line endings produce no changes.
 *
 * A line ending ends a line rather than starting a new one, so `"a\n"` is the single line `a`, and `""` has no lines at
 * all. This means two strings that only differ in a final line ending produce no changes either.
 */
internal class LineDiff(val expected: String, val actual: String) {
    sealed class Line {
        abstract val text: String

        /** A line both strings have. */
        data class Kept(override val text: String) : Line()

        /** A line only the expected string has. */
        data class Removed(override val text: String) : Line()

        /** A line only the actual string has. */
        data class Added(override val text: String) : Line()
    }

    private fun String.splitIntoLines(): List<String> {
        val lines = split(Regex("\r?\n"))
        return if (lines.last().isEmpty()) lines.dropLast(1) else lines
    }

    /**
     * Diffs two lists of lines with the standard longest common subsequence algorithm, keeping as many lines as
     * possible.
     *
     * See also: https://en.wikipedia.org/wiki/Longest_common_subsequence
     */
    private fun createLines(expected: List<String>, actual: List<String>): List<Line> {
        // Strings in a failing test usually differ in only a few lines, so set aside the matching start and end first,
        // to keep the table below small
        val shorterSize = minOf(expected.size, actual.size)
        var prefixSize = 0
        while (prefixSize < shorterSize && expected[prefixSize] == actual[prefixSize]) prefixSize++
        var suffixSize = 0
        while (
            suffixSize < shorterSize - prefixSize &&
            expected[expected.lastIndex - suffixSize] == actual[actual.lastIndex - suffixSize]
        ) suffixSize++

        val expectedMiddle = expected.subList(prefixSize, expected.size - suffixSize)
        val actualMiddle = actual.subList(prefixSize, actual.size - suffixSize)

        // commonCounts[e][a] is how many lines the rest of the two lists, from expectedMiddle[e] and actualMiddle[a] on,
        // can keep
        val commonCounts = Array(expectedMiddle.size + 1) { IntArray(actualMiddle.size + 1) }
        for (e in expectedMiddle.indices.reversed()) {
            for (a in actualMiddle.indices.reversed()) {
                commonCounts[e][a] = if (expectedMiddle[e] == actualMiddle[a]) {
                    commonCounts[e + 1][a + 1] + 1
                } else {
                    maxOf(commonCounts[e + 1][a], commonCounts[e][a + 1])
                }
            }
        }

        return buildList {
            expected.subList(0, prefixSize).forEach { add(Line.Kept(it)) }

            var e = 0
            var a = 0
            while (e < expectedMiddle.size && a < actualMiddle.size) {
                when {
                    expectedMiddle[e] == actualMiddle[a] -> {
                        add(Line.Kept(expectedMiddle[e]))
                        e++
                        a++
                    }
                    // On a tie, remove first, so a changed line shows its old version before its new one
                    commonCounts[e + 1][a] >= commonCounts[e][a + 1] -> {
                        add(Line.Removed(expectedMiddle[e]))
                        e++
                    }
                    else -> {
                        add(Line.Added(actualMiddle[a]))
                        a++
                    }
                }
            }
            expectedMiddle.subList(e, expectedMiddle.size).forEach { add(Line.Removed(it)) }
            actualMiddle.subList(a, actualMiddle.size).forEach { add(Line.Added(it)) }

            expected.subList(expected.size - suffixSize, expected.size).forEach { add(Line.Kept(it)) }
        }
    }

    /** Every line of both strings, in order, with each change's removed lines before its added ones. */
    val lines: List<Line> = createLines(expected.splitIntoLines(), actual.splitIntoLines())
}
