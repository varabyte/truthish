package com.varabyte.truthish.failure

import kotlin.reflect.KClass

internal fun stringifierFor(value: Any?): ValueStringifier {
    return when (value) {
        is ValueStringifier -> value // In case caller already wrapped `value` in a stringifier themselves
        is Char -> CharStringifier(value)
        is CharSequence -> StringStringifier(value)
        is Map<*, *> -> MapStringifier(value)
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

internal class CharStringifier(private val value: Char) : ValueStringifier(value) {
    override fun toString() = "'$value'"
}

internal class StringStringifier(private val value: CharSequence) : ValueStringifier(value) {
    override fun toString() = "\"$value\""
}

internal class IterableStringifier(private val value: Iterable<*>) : ValueStringifier(value) {
    override fun toString() = "[ ${value.joinToString(", ") { stringifierFor(it).toString() }} ]"
}

internal class MapStringifier(private val value: Map<*, *>) : ValueStringifier(value) {
    override fun toString() = "{ ${value.entries.joinToString(", ") { stringifierFor(it).toString() }} }"
}

// This should take precedence over IterableStringifier!
internal class SetStringifier(private val value: Set<*>) : ValueStringifier(value) {
    override fun toString() = "{ ${value.joinToString(", ") { stringifierFor(it).toString() }} }"
}

internal class KClassStringifier(private val value: KClass<*>) : ValueStringifier(value) {
    // Anonymous classes have no simple name
    override fun toString() = value.simpleName ?: value.toQualifiedString()
    override fun toDetailString() = value.toQualifiedString()
}
