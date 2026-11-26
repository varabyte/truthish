package com.varabyte.truthish.util.diff

/**
 * A line-by-line diff between two multi-line strings, e.g. for showing where they differ in a report.
 *
 * Lines are split on both `\n` and `\r\n`, so two strings that only differ in line endings produce no changes.
 *
 * A line ending ends a line rather than starting a new one, so `"a\n"` is the single line `a`, and `""` has no lines at
 * all. This means two strings that only differ in a final line ending produce no changes either.
 */
internal class LineDiff private constructor(
    val expected: String,
    val actual: String,
    /** Every line of both strings, in order, with each change's removed lines before its added ones. */
    val lines: List<Line>,
) {
    sealed class Line {
        abstract val text: String

        /** A line both strings have. */
        data class Kept(override val text: String) : Line()

        /** A line only the expected string has. */
        data class Removed(override val text: String) : Line()

        /** A line only the actual string has. */
        data class Added(override val text: String) : Line()
    }

    companion object {
        /**
         * The most cells the diff table may have, at 4 bytes each.
         *
         * Past this, a diff is too long to read in a report anyway, and the table could exhaust memory (two 20,000 line
         * strings would need over 1.5 GB).
         */
        private const val MAX_TABLE_CELLS = 1_000_000

        /**
         * Diffs [expected] and [actual], or returns `null` if they're too big to diff (see [MAX_TABLE_CELLS]).
         *
         * @param ignoreCase If true, lines that differ only in case count as unchanged. Since the two lines' text
         *   differs, an unchanged line always picks the expected line's text.
         */
        fun tryCreate(expected: String, actual: String, ignoreCase: Boolean = false): LineDiff? =
            createLines(expected.splitIntoLines(), actual.splitIntoLines(), ignoreCase)
                ?.let { LineDiff(expected, actual, it) }

        private fun String.splitIntoLines(): List<String> {
            val lines = split(Regex("\r?\n"))
            return if (lines.last().isEmpty()) lines.dropLast(1) else lines
        }

        /**
         * Diffs two lists of lines with the standard longest common subsequence algorithm, keeping as many lines as
         * possible, or returns `null` if the lists are too big to compare this way (see [MAX_TABLE_CELLS]).
         *
         * See also: https://en.wikipedia.org/wiki/Longest_common_subsequence
         */
        private fun createLines(expected: List<String>, actual: List<String>, ignoreCase: Boolean): List<Line>? {
            fun String.isSameLineAs(other: String) = equals(other, ignoreCase)

            // Strings in a failing test usually differ in only a few lines, so set aside the matching start and end
            // first, to keep the table below small
            val shorterSize = minOf(expected.size, actual.size)
            var prefixSize = 0
            while (prefixSize < shorterSize && expected[prefixSize].isSameLineAs(actual[prefixSize])) prefixSize++
            var suffixSize = 0
            while (
                suffixSize < shorterSize - prefixSize &&
                expected[expected.lastIndex - suffixSize].isSameLineAs(actual[actual.lastIndex - suffixSize])
            ) suffixSize++

            val expectedMiddle = expected.subList(prefixSize, expected.size - suffixSize)
            val actualMiddle = actual.subList(prefixSize, actual.size - suffixSize)

            if (expectedMiddle.size.toLong() * actualMiddle.size > MAX_TABLE_CELLS) return null

            // commonCounts[e][a] is how many lines the rest of the two lists, from expectedMiddle[e] and
            // actualMiddle[a] on, can keep
            val commonCounts = Array(expectedMiddle.size + 1) { IntArray(actualMiddle.size + 1) }
            for (e in expectedMiddle.indices.reversed()) {
                for (a in actualMiddle.indices.reversed()) {
                    commonCounts[e][a] = if (expectedMiddle[e].isSameLineAs(actualMiddle[a])) {
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
                        expectedMiddle[e].isSameLineAs(actualMiddle[a]) -> {
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
    }

    /**
     * Diffs [expected] and [actual].
     *
     * @param ignoreCase See [tryCreate].
     * @throws IllegalArgumentException if they're too big to diff. Use [tryCreate] if that can happen.
     */
    constructor(expected: String, actual: String, ignoreCase: Boolean = false) : this(
        expected,
        actual,
        createLines(expected.splitIntoLines(), actual.splitIntoLines(), ignoreCase)
            ?: throw IllegalArgumentException("The strings are too big to diff"),
    )
}
