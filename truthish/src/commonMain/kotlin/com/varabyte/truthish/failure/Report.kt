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
 *
 * @see FailureStrategy
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
                builder.append(details.render(pair))
            }
        }
        builder.append("\n")
        return builder.toString()
    }
}

/** Two rendered values that were expected to be equal, which a test runner can show as a diff. */
internal class ComparedValues(val expected: String, val actual: String)

/**
 * The "Expected" and "But was" values as rendered in this report, if it has both.
 *
 * Labels with a suffix (e.g. "Expected greater than") describe a relation rather than an expected value, so they don't
 * count. Values that render identically (e.g. two equal but distinct instances) don't either, since a diff between them
 * would show nothing.
 */
private fun Report.findComparedValues(): ComparedValues? {
    val expected = details.items.find { it.first == DetailsFor.EXPECTED } ?: return null
    val actual = details.items.find { it.first == DetailsFor.BUT_WAS } ?: return null
    return ComparedValues(details.render(expected, truncate = false), details.render(actual, truncate = false))
        .takeIf { it.expected != it.actual }
}

internal fun Report.toAssertionError(): AssertionError = createAssertionError(findComparedValues())

private fun Details.render(item: Pair<String, Any?>, truncate: Boolean = true): String {
    val value = stringifierFor(item.second).render(truncate)
    return extras[item.first]?.let { extra -> "$value ($extra)" } ?: value
}

/**
 * A collection of key/value pairs which should be included in a table-like report.
 *
 * @property extras Optional additional information which, if specified, should be appended after the value
 *   somehow. The key for the extra information should be the same as the key for the details item. For example,
 *   the detail ("Expected" to "xyz") can be decorated with extra information by adding the extra entry
 *   ("Expected" to "Type is `ClassXyz`").
 */
class Details internal constructor(
    items: List<Pair<String, Any?>>,
    internal val extras: Map<String, String>,
) {
    constructor(items: List<Pair<String, Any?>> = emptyList()) : this(items, emptyMap())

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
    const val MESSAGE = "Message"
    const val NAME = "Name"
    const val VALUE = "Value"
    const val EXPECTED = "Expected"
    const val BUT_WAS = "But was"
    const val AT = "At"
    const val MISSING = "Missing"
    const val EXTRANEOUS = "Extraneous"
    const val UNEXPECTED = "Unexpected"
    const val DUPLICATES = "Duplicates"
    const val IN_ORDER_SO_FAR = "In order so far"
    const val OUT_OF_ORDER = "Out of order"

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

    private fun createExpectedActualDetails(expectedLabel: String, expected: Any?, actual: Any?): Details {
        val extras = buildMap<String, String> {
            if (expected != null && actual != null) {
                val expectedStringifier = stringifierFor(expected)
                val actualStringifier = stringifierFor(actual)
                // Compare full renderings, since otherwise different values might look the same after relevant elements
                // were elided.
                val expectedString = expectedStringifier.render(truncate = false)
                val actualString = actualStringifier.render(truncate = false)

                var isOutputAmbiguous = expectedString == actualString

                // String output adds surrounding quotes, but that can still result in confusing error like
                // Expected: "x"    But was: x
                // so handle those cases too.
                if (!isOutputAmbiguous && expected is String && actual !is String && expected == actualString) {
                    isOutputAmbiguous = true
                }
                if (!isOutputAmbiguous && expected !is String && actual is String && expectedString == actual) {
                    isOutputAmbiguous = true
                }

                if (isOutputAmbiguous) {
                    val expectedDetail = expectedStringifier.toDetailString()
                    val actualDetail = actualStringifier.toDetailString()
                    if (expectedDetail != actualDetail) {
                        put(expectedLabel, expectedDetail)
                        put(BUT_WAS, actualDetail)
                    }
                    // If the values are equal, the failure is about something we can't show
                    else if (expected != actual) {
                        put(BUT_WAS, "a different value that prints the same")
                    }
                }
            }
        }
        return Details(listOf(expectedLabel to expected, BUT_WAS to actual), extras)
    }

    /**
     * A detail list useful when asserting about an expected value vs. an actual one.
     *
     * On platforms where the test runner supports it (e.g. IntelliJ on the JVM), failures with these details can be
     * shown as a diff between the two values.
     */
    fun expectedActual(expected: Any?, actual: Any?): Details {
        return createExpectedActualDetails(EXPECTED, expected, actual)
    }

    /**
     * Like the other [expectedActual] method, except you can add more details to the "Expected"
     * label. For example, "Expected greater than:" vs just "Expected"
     *
     * For consistency / readability, [expectedSuffix] should be lower-case. A space will automatically be inserted
     *   between the "expected" label and the extra information.
     */
    fun expectedActual(expectedSuffix: String, expected: Any?, actual: Any?): Details {
        return createExpectedActualDetails("$EXPECTED $expectedSuffix", expected, actual)
    }
}
