package com.varabyte.truthish.failure

import kotlin.reflect.KClass

/**
 * Extra information about how to render a value, beyond the value itself. Stringifiers that don't need a piece of it
 * ignore it.
 *
 * @property anchor Where to truncate the value, if it's a long list.
 */
internal class StringifierContext(val anchor: Anchor? = null)

internal fun stringifierFor(value: Any?, context: StringifierContext? = null): ValueStringifier {
    return when (value) {
        is ValueStringifier -> value // In case caller already wrapped `value` in a stringifier themselves
        is Char -> CharStringifier(value)
        is CharSequence -> StringStringifier(value)
        is Map<*, *> -> MapStringifier(value)
        is Map.Entry<*, *> -> PairStringifier(value, value.key, value.value)
        is Pair<*, *> -> PairStringifier(value, value.first, value.second)
        is Triple<*, *, *> -> TripleStringifier(value)
        is Set<*> -> SetStringifier(value)
        is Iterable<*> -> IterableStringifier(value, context?.anchor)
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
 * How [joinValues] shortens a long collection.
 */
private sealed interface Truncation {
    object None : Truncation
    object BothEnds : Truncation
    class Anchored(val anchor: Anchor) : Truncation
}

/**
 * Joins these values, each formatted with its own stringifier, between [open] and [close], e.g. `[ 1, 2 ]` or `[]`.
 *
 * @param padded Whether to put spaces inside [open] and [close]. Collections use them, while fixed-size groups like
 *   triples read better tight, e.g. `(1, 2, 3)`.
 */
private fun Iterable<*>.joinValues(
    open: String,
    close: String,
    truncation: Truncation,
    padded: Boolean = true,
): String {
    val values = toList()
    if (values.isEmpty()) return "$open$close"

    val truncator = Truncator.forReports
    val truncatedValues = when (truncation) {
        Truncation.None -> listOf(TruncatedPart.Shown(values))
        Truncation.BothEnds -> truncator.truncate(values, firstAnchor = 0, secondAnchor = values.lastIndex)
        is Truncation.Anchored -> truncator.truncate(values, truncation.anchor)
    }
    // Nested collections don't share their parent's anchor, since its index doesn't apply to them
    val truncateNested = truncation != Truncation.None
    val parts = truncatedValues.flatMap { part ->
        when (part) {
            is TruncatedPart.Shown -> part.values.map { stringifierFor(it).render(truncateNested) }
            is TruncatedPart.Hidden -> listOf("… (${part.count} more)")
        }
    }

    val padding = if (padded) " " else ""
    return parts.joinToString(", ", prefix = "$open$padding", postfix = "$padding$close")
}

// For simple boolean cases, we only consider truncating long collections from both ends or not truncating at all.
private fun Boolean.toTruncation() = if (this) Truncation.BothEnds else Truncation.None

/**
 * @param anchor If set, a long list is truncated around it, rather than the default behavior of truncating from both
 *   ends.
 */
internal class IterableStringifier(
    private val value: Iterable<*>,
    private val anchor: Anchor? = null,
) : ValueStringifier(value) {
    override fun render(truncate: Boolean): String {
        val truncation = when {
            !truncate -> Truncation.None
            anchor != null -> Truncation.Anchored(anchor)
            else -> Truncation.BothEnds
        }
        return value.joinValues("[", "]", truncation)
    }
}

internal class MapStringifier(private val value: Map<*, *>) : ValueStringifier(value) {
    override fun render(truncate: Boolean) = value.entries.joinValues("{", "}", truncate.toTruncation())
}

// This should take precedence over IterableStringifier!
internal class SetStringifier(private val value: Set<*>) : ValueStringifier(value) {
    override fun render(truncate: Boolean) = value.joinValues("{", "}", truncate.toTruncation())
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
    override fun render(truncate: Boolean) =
        value.toList().joinValues("(", ")", truncate.toTruncation(), padded = false)
}

internal class KClassStringifier(private val value: KClass<*>) : ValueStringifier(value) {
    // Anonymous classes have no simple name
    override fun render(truncate: Boolean) = value.simpleName ?: value.toQualifiedString()
    override fun toDetailString() = value.toQualifiedString()
}
