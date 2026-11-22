package com.varabyte.truthish.failure

import kotlin.reflect.KClass

internal fun stringifierFor(value: Any?): ValueStringifier {
    return when (value) {
        is ValueStringifier -> value // In case caller already wrapped `value` in a stringifier themselves
        is Char -> CharStringifier(value)
        is CharSequence -> StringStringifier(value)
        is Map<*, *> -> MapStringifier(value)
        is Map.Entry<*, *> -> PairStringifier(value, value.key, value.value)
        is Pair<*, *> -> PairStringifier(value, value.first, value.second)
        is Triple<*, *, *> -> TripleStringifier(value)
        is Set<*> -> SetStringifier(value)
        is Iterable<*> -> IterableStringifier(value)
        is KClass<*> -> KClassStringifier(value)
        else -> AnyStringifier(value)
    }
}

/**
 * Converts a class into its most specific name available on this platform, e.g. to tell apart two classes that share a
 * simple name.
 */
internal expect fun KClass<*>.toQualifiedString(): String

/**
 * The most elements a collection shows before the rest are summarized as `… (N more)`.
 */
// NOTE: An odd number was intentionally chosen so that truncations that happen around a pivot point can show the same
// number of elements before and after.
private const val MAX_ELEMENTS = 11

/**
 * A base class for rendering a value in a report.
 */
internal sealed class ValueStringifier(private val value: Any?) {
    /**
     * Renders the value.
     *
     * @param truncate Whether to shorten large values (e.g. long collections) to keep reports readable. Values
     *   handed to a diff viewer are rendered in full, since it can show them better. It is up to each child stringifier
     *   to decide how to handle this -- most can simply ignore it.
     */
    abstract fun render(truncate: Boolean): String

    final override fun toString() = render(truncate = true)

    /**
     * Extra detail to tell this value apart from another value that prints the same.
     */
    open fun toDetailString(): String = "Type: `${value?.let { it::class.simpleName }}`"
}

/**
 * A default Stringifier that can handle any case.
 */
internal class AnyStringifier(private val value: Any?) : ValueStringifier(value) {
    override fun render(truncate: Boolean) = value?.let { "$it" } ?: "(null)"
}

private enum class Quote(val char: Char) {
    SINGLE('\''),
    DOUBLE('"'),
}

/**
 * Escapes this character the way it would be written inside a Kotlin literal delimited by [quote].
 *
 * This makes invisible characters like tabs, newlines and ANSI escape codes show up in reports, and lets a reported value
 * be pasted straight back into a test.
 */
private fun Char.toEscapedString(quote: Quote): String {
    return when {
        this == '\t' -> "\\t"
        this == '\n' -> "\\n"
        this == '\r' -> "\\r"
        this == '\b' -> "\\b"
        this == '\\' -> "\\\\"
        this == quote.char -> "\\$this"
        isISOControl() -> "\\u" + code.toString(16).uppercase().padStart(4, '0')
        else -> toString()
    }
}

internal class CharStringifier(private val value: Char) : ValueStringifier(value) {
    override fun render(truncate: Boolean) = "'${value.toEscapedString(Quote.SINGLE)}'"
}

internal class StringStringifier(private val value: CharSequence) : ValueStringifier(value) {
    override fun render(truncate: Boolean) = buildString {
        append('"')
        value.forEach { append(it.toEscapedString(Quote.DOUBLE)) }
        append('"')
    }
}

/**
 * Joins these values, each formatted with its own stringifier, between [open] and [close], e.g. `[ 1, 2 ]` or `[]`.
 *
 * @param truncate Whether to show only the first [MAX_ELEMENTS] values, followed by a count of the rest.
 * @param padded Whether to put spaces inside [open] and [close]. Collections use them, while fixed-size groups like
 *   triples read better tight, e.g. `(1, 2, 3)`.
 */
private fun Iterable<*>.joinValues(open: String, close: String, truncate: Boolean, padded: Boolean = true): String {
    val values = toList()
    if (values.isEmpty()) return "$open$close"

    val shown = if (truncate && values.size > MAX_ELEMENTS) values.take(MAX_ELEMENTS) else values
    val parts = shown.map { stringifierFor(it).render(truncate) }.toMutableList()
    if (shown.size < values.size) {
        parts.add("… (${values.size - shown.size} more)")
    }

    val padding = if (padded) " " else ""
    return parts.joinToString(", ", prefix = "$open$padding", postfix = "$padding$close")
}

internal class IterableStringifier(private val value: Iterable<*>) : ValueStringifier(value) {
    override fun render(truncate: Boolean) = value.joinValues("[", "]", truncate = truncate)
}

internal class MapStringifier(private val value: Map<*, *>) : ValueStringifier(value) {
    override fun render(truncate: Boolean) = value.entries.joinValues("{", "}", truncate = truncate)
}

// This should take precedence over IterableStringifier!
internal class SetStringifier(private val value: Set<*>) : ValueStringifier(value) {
    override fun render(truncate: Boolean) = value.joinValues("{", "}", truncate = truncate)
}

/**
 * Formats a pair (or map entry) the way it's usually written in Kotlin code, e.g. `"a" to 1`.
 */
internal class PairStringifier(
    value: Any,
    private val first: Any?,
    private val second: Any?,
) : ValueStringifier(value) {
    override fun render(truncate: Boolean) =
        "${stringifierFor(first).wrapInParensIfNecessary(truncate)} to " +
            stringifierFor(second).wrapInParensIfNecessary(truncate)

    // A nested value that is itself a pair needs parentheses to stay unambiguous, e.g. `(1 to 2) to 3`
    private fun ValueStringifier.wrapInParensIfNecessary(truncate: Boolean): String {
        val rendered = render(truncate)
        return if (this is PairStringifier) "($rendered)" else rendered
    }
}

internal class TripleStringifier(private val value: Triple<*, *, *>) : ValueStringifier(value) {
    override fun render(truncate: Boolean) = value.toList().joinValues("(", ")", truncate = truncate, padded = false)
}

internal class KClassStringifier(private val value: KClass<*>) : ValueStringifier(value) {
    // Anonymous classes have no simple name
    override fun render(truncate: Boolean) = value.simpleName ?: value.toQualifiedString()
    override fun toDetailString() = value.toQualifiedString()
}
