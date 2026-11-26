package com.varabyte.truthish.failure

import com.varabyte.truthish.util.diff.LineDiff
import com.varabyte.truthish.util.truncate.Truncation

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
            // A multi-line value's later lines line up with its first one, so the table keeps its shape
            val valueIndent = " ".repeat(longestKey + ": ".length)

            detailItems.forEachIndexed { index, pair ->
                if (index > 0) {
                    builder.append("\n")
                }
                builder.append(pair.first)
                builder.append(" ".repeat(longestKey - pair.first.length)).append(": ")
                details.render(pair).lines().forEachIndexed { lineIndex, line ->
                    if (lineIndex > 0) {
                        builder.append("\n")
                        // Leave blank lines empty instead of filling them with trailing spaces
                        if (line.isNotEmpty()) builder.append(valueIndent)
                    }
                    builder.append(line)
                }
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
 * Multi-line strings are an exception: they're passed as is, unescaped, so a diff view can compare them line by line.
 *
 * Expected labels with a suffix (e.g. "Expected greater than") describe a relation rather than an expected value, so
 * they don't count. Values that render identically (e.g. two equal but distinct instances) don't either, since a diff
 * between them would show nothing.
 */
private fun Report.findComparedValues(): ComparedValues? {
    details.items.firstNotNullOfOrNull { it.second as? LineDiff }?.let { diff ->
        return ComparedValues(diff.expected, diff.actual)
    }
    val expected = details.items.find { it.first == DetailsFor.EXPECTED } ?: return null
    val actual = details.items.find { it.first == DetailsFor.BUT_WAS } ?: return null
    val expectedValue = expected.second
    val actualValue = actual.second
    if (expectedValue is CharSequence && actualValue is CharSequence &&
        (expectedValue.contains('\n') || actualValue.contains('\n'))
    ) {
        // Pass the text as is. The IDE's diff viewer can handle whatever you throw at it.
        return ComparedValues(expectedValue.toString(), actualValue.toString())
            .takeIf { it.expected != it.actual }
    }
    return ComparedValues(
        details.render(expected, details.context(expected.first).copy(truncation = Truncation.None)),
        details.render(actual, details.context(actual.first).copy(truncation = Truncation.None)),
    )
        .takeIf { it.expected != it.actual }
}

internal fun Report.toAssertionError(): AssertionError = createAssertionError(findComparedValues())

/**
 * Renders [item] the way the report shows it, or with [context] instead, e.g. to show it in full.
 */
private fun Details.render(
    item: Pair<String, Any?>,
    context: StringifierContext = context(item.first),
): String {
    val (label, value) = item
    val rendered = ValueStringifier.createFor(value, context).render()
    val extra = extras[label] ?: return rendered
    // After a multi-line value, an extra on the last line would read as part of it
    return if (rendered.contains('\n')) "$rendered\n($extra)" else "$rendered ($extra)"
}

/**
 * A collection of key/value pairs which should be included in a table-like report.
 *
 * @property extras Optional additional information which, if specified, should be appended after the value
 *   somehow. The key for the extra information should be the same as the key for the details item. For example,
 *   the detail ("Expected" to "xyz") can be decorated with extra information by adding the extra entry
 *   ("Expected" to "Type is `ClassXyz`").
 * @property contexts Optional extra information about how to render values, keyed like [extras], e.g. where to
 *   truncate a long list.
 */
class Details internal constructor(
    items: List<Pair<String, Any?>>,
    internal val extras: Map<String, String>,
    contexts: Map<String, StringifierContext> = emptyMap(),
) {
    constructor(items: List<Pair<String, Any?>> = emptyList()) : this(items, emptyMap())

    private val _items = items.toMutableList()
    private val notes = mutableListOf<String>()

    /** All rows, ending with a single Note row that joins every note added via [addNote], if any. */
    val items: List<Pair<String, Any?>>
        get() {
            if (notes.isEmpty()) return _items
            val noteText = notes.joinToString("; ").replaceFirstChar { it.uppercase() }
            return _items + (DetailsFor.NOTE to AnyStringifier(noteText))
        }

    private val _contexts = contexts.toMutableMap()
    internal val contexts: Map<String, StringifierContext> = _contexts

    /**
     * Adds a note, which reports show in a single Note row at the end, e.g. "Line endings also differ; expected ends
     * with a newline but actual doesn't".
     *
     * Write [note] in lowercase; the row capitalizes its first letter. Use this rather than adding a [DetailsFor.NOTE]
     * row directly.
     */
    fun addNote(note: String) {
        notes.add(note)
    }

    /**
     * @param context Optional [StringifierContext] associated with the value.
     */
    fun add(index: Int, element: Pair<String, Any?>, context: StringifierContext = StringifierContext.Default) {
        _items.add(index, element)
        _contexts[element.first] = context
    }

    /**
     * @param context Optional [StringifierContext] associated with the value.
     */
    fun add(element: Pair<String, Any?>, context: StringifierContext = StringifierContext.Default) {
        _items.add(element)
        _contexts[element.first] = context
    }
}

private fun Details.context(label: String) = contexts[label] ?: StringifierContext.Default

fun Details.find(key: String): Any? {
    return items.find { it.first == key }?.second
}

/**
 * Helpful utility methods providing detail lists for common scenarios.
 */
object DetailsFor {
    const val MESSAGE = "Message"
    const val NAME = "Name"
    const val VALUE_OF = "Value of"
    const val VALUE = "Value"
    const val EXPECTED = "Expected"
    const val BUT_WAS = "But was"
    const val AT = "At"
    const val MISSING = "Missing"
    const val EXTRANEOUS = "Extraneous"
    const val UNEXPECTED = "Unexpected"
    const val DUPLICATES = "Duplicates"
    const val OUT_OF_ORDER = "Out of order"
    const val TESTING_WHETHER = "Testing whether"
    const val POSSIBLY_MISSING = "Possibly missing"
    const val POSSIBLY_EXTRANEOUS = "Possibly extraneous"
    const val DIFF = "Diff"
    const val NOTE = "Note"

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

    /**
     * The shared implementation of both [expectedActual] methods.
     *
     * @param expectedSuffix Extra text for the "Expected" label, e.g. "greater than", or `null` for none.
     */
    private fun createExpectedActualDetails(
        expectedSuffix: String?,
        expected: Any?,
        actual: Any?,
        expectedContext: StringifierContext,
        actualContext: StringifierContext,
    ): Details {
        val expectedLabel = if (expectedSuffix != null) "$EXPECTED $expectedSuffix" else EXPECTED
        val contexts = mapOf(expectedLabel to expectedContext, BUT_WAS to actualContext)
        val extras = buildMap<String, String> {
            if (expected != null && actual != null) {
                // Compare full renderings, since otherwise different values might look the same after relevant elements
                // were elided.
                fun contextWithFullRender(label: String) =
                    (contexts[label] ?: StringifierContext.Default).copy(truncation = Truncation.None)
                val expectedStringifier = ValueStringifier.createFor(expected, contextWithFullRender(expectedLabel))
                val actualStringifier = ValueStringifier.createFor(actual, contextWithFullRender(BUT_WAS))
                val expectedString = expectedStringifier.render()
                val actualString = actualStringifier.render()

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
        return Details(listOf(expectedLabel to expected, BUT_WAS to actual), extras, contexts)
    }

    /**
     * A detail list useful when asserting about an expected value vs. an actual one.
     *
     * On platforms where the test runner supports it (e.g. IntelliJ on the JVM), failures with these details can be
     * shown as a diff between the two values.
     *
     * @param expectedContext Optional [StringifierContext] associated with [expected].
     * @param actualContext Optional [StringifierContext] associated with [actual].
     */
    fun expectedActual(
        expected: Any?,
        actual: Any?,
        expectedContext: StringifierContext = StringifierContext.Default,
        actualContext: StringifierContext = StringifierContext.Default,
    ): Details {
        return createExpectedActualDetails(expectedSuffix = null, expected, actual, expectedContext, actualContext)
    }

    /**
     * Like the other [expectedActual] method, except you can add more details to the "Expected"
     * label. For example, "Expected greater than:" vs just "Expected"
     *
     * For consistency / readability, [expectedSuffix] should be lower-case. A space will automatically be inserted
     *   between the "expected" label and the extra information.
     *
     * @param expectedContext Optional [StringifierContext] associated with [expected].
     * @param actualContext Optional [StringifierContext] associated with [actual].
     */
    fun expectedActual(
        expectedSuffix: String,
        expected: Any?,
        actual: Any?,
        expectedContext: StringifierContext = StringifierContext.Default,
        actualContext: StringifierContext = StringifierContext.Default,
    ): Details {
        return createExpectedActualDetails(expectedSuffix, expected, actual, expectedContext, actualContext)
    }
}
