package com.varabyte.truthish.failure

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
 * and any specific details for this particular report should be specified in the [details] parameter.
 */
class Report(private val summary: String, details: Details? = null) {
    internal val details = details ?: Details()
    override fun toString(): String {
        val builder = StringBuilder(summary)
        val detailItems = details.items
        if (detailItems.isNotEmpty()) {
            builder.append("\n\n")
            val longestKey = detailItems.maxOf { it.first.length }

            detailItems.forEachIndexed { index, pair ->
                if (index > 0) {
                    builder.append("\n")
                }
                builder.append(pair.first)
                builder.append(" ".repeat(longestKey - pair.first.length)).append(": ")
                builder.append(stringifierFor(pair.second))
                details.extras[pair.first]?.let { extra -> builder.append(" ($extra)") }
            }
        }
        builder.append("\n")
        return builder.toString()
    }
}

/**
 * A collection of key/value pairs which should be included in a table-like report.
 *
 * @property extras Optional additional information which, if specified, should be appended after the value
 *   somehow. The key for the extra information should be the same as the key for the details item. For example,
 *   the detail ("Expected" to "xyz") can be decorated with extra information by adding the extra entry
 *   ("Expected" to "Type is `ClassXyz`").
 */
class Details(
    items: List<Pair<String, Any?>> = emptyList(),
    internal val extras: Map<String, String> = emptyMap()
) {
    private val _items = items.toMutableList()
    val items: List<Pair<String, Any?>> = _items

    fun add(index: Int, element: Pair<String, Any?>) {
        _items.add(index, element)
    }

    fun add(element: Pair<String, Any?>) {
        _items.add(element)
    }
}

fun Details.find(key: String): Any? {
    return items.find { it.first == key }?.second
}

/**
 * Helpful utility methods providing detail lists for common scenarios.
 */
object DetailsFor {
    const val VALUE = "Value"
    const val EXPECTED = "Expected"
    const val BUT_WAS = "But was"
    const val AT = "At"

    /**
     * A detail list useful when asserting about the state of a single value, e.g. it was
     * expected to be `null` but instead it's *(some value)*.
     */
    fun actual(actual: Any?): Details {
        return Details(listOf(VALUE to actual))
    }

    /**
     * A detail list useful when asserting about something expected not happening, e.g.
     * it was expected that an IoException would be thrown but one wasn't.
     */
    fun expected(expected: Any?): Details {
        return Details(listOf(EXPECTED to expected))
    }

    private fun createExpectedActualExtrasFor(
        expected: Pair<String, Any?>,
        actual: Pair<String, Any?>
    ): Map<String, String> {
        return buildMap {
            val expectedValue = expected.second
            val actualValue = actual.second
            if (expectedValue != null && actualValue != null) {
                val expectedStringifier = stringifierFor(expectedValue)
                val actualStringifier = stringifierFor(actualValue)

                var isOutputAmbiguous = expectedStringifier.toString() == actualStringifier.toString()

                // String output adds surrounding quotes, but that can still result in confusing error like
                // Expected: "x"    But was: x
                // so handle those cases too.
                if (!isOutputAmbiguous && expectedValue is String && actualValue !is String && expectedValue == actualStringifier.toString()) {
                    isOutputAmbiguous = true
                }
                if (!isOutputAmbiguous && expectedValue !is String && actualValue is String && expectedStringifier.toString() == actualValue) {
                    isOutputAmbiguous = true
                }

                if (isOutputAmbiguous) {
                    put(EXPECTED, "Type: `${expectedValue::class.simpleName}`")
                    put(BUT_WAS, "Type: `${actualValue::class.simpleName}`")
                }
            }
        }
    }

    /**
     * A detail list useful when asserting about an expected value vs. an actual one.
     */
    fun expectedActual(expected: Any?, actual: Any?): Details {
        val items = listOf(
            EXPECTED to expected,
            BUT_WAS to actual
        )
        return Details(items, createExpectedActualExtrasFor(items[0], items[1]))
    }

    /**
     * Like the other [expectedActual] method, except you can add more details to the "Expected"
     * label. For example, "Expected greater than:" vs just "Expected"
     *
     * For consistency / readability, [expectedSuffix] should be lower-case. A space will automatically be inserted
     *   between the "expected" label and the extra information.
     */
    fun expectedActual(expectedSuffix: String, expected: Any?, actual: Any?): Details {
        val items = listOf(
            "$EXPECTED $expectedSuffix" to expected,
            BUT_WAS to actual
        )
        return Details(items, createExpectedActualExtrasFor(items[0], items[1]))
    }
}
