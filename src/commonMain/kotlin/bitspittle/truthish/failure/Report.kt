package bitspittle.truthish.failure

/**
 * Create a report which consists of a single summary line and an optional list of details.
 *
 * The report's [toString] is overloaded so it can be used directly in [println] calls.
 *
 * Use a [FailureStrategy] to handle a report.
 *
 * A sample report's output might look something like
 *
 * ```
 Two values did not equal each other
 *
 * Expected: 25
 * But was : 24
 * ```
 *
 * or
 *
 * ```
 * A collection was missing at least one item
 *
 * Missing : [ GREEN ]
 * Expected: [ RED, GREEN, BLUE ]
 * But was : [ RED, BLUE ]
 * ```
 *
 * @param summary A one-line summary of this report. This should be a generic, re-usable message,
 * and any specific details for this particular report should be highlighted in the [details].]
 *
 * @param details An optional, ordered list of key / value pairs to be shown as interesting
 * details. Details can be added later.
 */
class Report(private val summary: String, details: List<Pair<String, Any?>> = listOf()) {
    val details = details.toMutableList()

    override fun toString(): String {
        val builder = StringBuilder(summary)
        if (details.size > 0) {
            builder.append("\n\n")
            val longestKey = details.map { it.first.length }.max()!!

            details.forEachIndexed { index, pair ->
                if (index > 0) {
                    builder.append("\n")
                }
                builder.append(pair.first)
                builder.append(" ".repeat(longestKey - pair.first.length)).append(": ")
                builder.append(stringifierFor(pair.second))
            }
        }
        builder.append("\n")
        return builder.toString()
    }
}

/**
 * Helpful utility methods providing detail lists for common scenarios.
 */
object DetailsFor {
    private const val VALUE = "Value"
    private const val EXPECTED = "Expected"
    private const val BUT_WAS = "But was"

    /**
     * A detail list useful when asserting about the state of a single value, e.g. it was
     * expected to be `null` but instead it's *(some value)*.
     */
    fun actual(actual: Any?): MutableList<Pair<String, Any?>> {
        return mutableListOf(
            VALUE to actual
        )
    }

    /**
     * A detail list useful when asserting about something expected not happening, e.g.
     * it was expected that an IoException would be thrown but one wasn't.
     */
    fun expected(expected: Any?): MutableList<Pair<String, Any?>> {
        return mutableListOf(
            EXPECTED to expected
        )
    }

    /**
     * A detail list useful when asserting about an expected value vs. an actual one.
     */
    fun expectedActual(expected: Any?, actual: Any?): MutableList<Pair<String, Any?>> {
        return mutableListOf(
            EXPECTED to expected,
            BUT_WAS to actual
        )
    }

    /**
     * Like the other [expectedActual] method, except you can add more details to the "Expected"
     * label. For example, "Expected greater than:" vs just "Expected"
     *
     * For consistency / readability, [additionalInfo] should be lower-case.
     */
    fun expectedActual(additionalInfo: String, expected: Any?, actual: Any?): List<Pair<String, ValueStringifier>> {
        return listOf(
            "$EXPECTED $additionalInfo" to stringifierFor(expected),
            BUT_WAS to stringifierFor(actual)
        )
    }
}