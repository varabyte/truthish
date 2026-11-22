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
 * A base class which forces subclasses to override their [toString] method.
 */
internal sealed class ValueStringifier(private val value: Any?) {
    abstract override fun toString(): String

    /**
     * Extra detail to tell this value apart from another value that prints the same.
     */
    open fun toDetailString(): String = "Type: `${value?.let { it::class.simpleName }}`"
}

/**
 * A default Stringifier that can handle any case.
 */
internal class AnyStringifier(private val value: Any?) : ValueStringifier(value) {
    override fun toString() = value?.let { "$it" } ?: "(null)"
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
    override fun toString() = "'${value.toEscapedString(Quote.SINGLE)}'"
}

internal class StringStringifier(private val value: CharSequence) : ValueStringifier(value) {
    override fun toString() = buildString {
        append('"')
        value.forEach { append(it.toEscapedString(Quote.DOUBLE)) }
        append('"')
    }
}

/**
 * Joins these values, each formatted with its own stringifier, between [open] and [close], e.g. `[ 1, 2 ]` or `[]`.
 *
 * @param padded Whether to put spaces inside [open] and [close]. Collections use them, while fixed-size groups like
 *   triples read better tight, e.g. `(1, 2, 3)`.
 */
private fun Iterable<*>.joinValues(open: String, close: String, padded: Boolean = true): String {
    if (none()) return "$open$close"
    val padding = if (padded) " " else ""
    return joinToString(", ", prefix = "$open$padding", postfix = "$padding$close") { stringifierFor(it).toString() }
}

internal class IterableStringifier(private val value: Iterable<*>) : ValueStringifier(value) {
    override fun toString() = value.joinValues("[", "]")
}

internal class MapStringifier(private val value: Map<*, *>) : ValueStringifier(value) {
    override fun toString() = value.entries.joinValues("{", "}")
}

// This should take precedence over IterableStringifier!
internal class SetStringifier(private val value: Set<*>) : ValueStringifier(value) {
    override fun toString() = value.joinValues("{", "}")
}

/**
 * Formats a pair (or map entry) the way it's usually written in Kotlin code, e.g. `"a" to 1`.
 */
internal class PairStringifier(
    value: Any,
    private val first: Any?,
    private val second: Any?,
) : ValueStringifier(value) {
    override fun toString() =
        "${stringifierFor(first).wrapInParensIfNecessary()} to ${stringifierFor(second).wrapInParensIfNecessary()}"

    // A nested value that is itself a pair needs parentheses to stay unambiguous, e.g. `(1 to 2) to 3`
    private fun ValueStringifier.wrapInParensIfNecessary(): String {
        return if (this is PairStringifier) "($this)" else toString()
    }
}

internal class TripleStringifier(private val value: Triple<*, *, *>) : ValueStringifier(value) {
    override fun toString() = value.toList().joinValues("(", ")", padded = false)
}

internal class KClassStringifier(private val value: KClass<*>) : ValueStringifier(value) {
    // Anonymous classes have no simple name
    override fun toString() = value.simpleName ?: value.toQualifiedString()
    override fun toDetailString() = value.toQualifiedString()
}
