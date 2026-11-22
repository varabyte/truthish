package com.varabyte.truthish.failure

internal fun stringifierFor(value: Any?): ValueStringifier {
    return when (value) {
        is ValueStringifier -> value // In case caller already wrapped `value` in a stringifier themselves
        is Char -> CharStringifier(value)
        is CharSequence -> StringStringifier(value)
        is Map<*, *> -> MapStringifier(value)
        is Set<*> -> SetStringifier(value)
        is Iterable<*> -> IterableStringifier(value)
        else -> AnyStringifier(value)
    }
}

/**
 * A base class which forces subclasses to override their [toString] method.
 */
internal sealed class ValueStringifier {
    abstract override fun toString(): String
}

/**
 * A default Stringifier that can handle any case.
 */
internal class AnyStringifier(private val value: Any?) : ValueStringifier() {
    override fun toString() = value?.let { "$it" } ?: "(null)"
}

internal class CharStringifier(private val value: Char) : ValueStringifier() {
    override fun toString() = "'$value'"
}

internal class StringStringifier(private val value: CharSequence) : ValueStringifier() {
    override fun toString() = "\"$value\""
}

internal class IterableStringifier(private val value: Iterable<*>) : ValueStringifier() {
    override fun toString() = "[ ${value.joinToString(", ") { stringifierFor(it).toString() }} ]"
}

internal class MapStringifier(private val value: Map<*, *>) : ValueStringifier() {
    override fun toString() = "{ ${value.entries.joinToString(", ") { stringifierFor(it).toString() }} }"
}

// This should take precedence over IterableStringifier!
internal class SetStringifier(private val value: Set<*>) : ValueStringifier() {
    override fun toString() = "{ ${value.joinToString(", ") { stringifierFor(it).toString() }} }"
}