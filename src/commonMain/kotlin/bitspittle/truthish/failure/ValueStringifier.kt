package bitspittle.truthish.failure

internal fun stringifierFor(value: Any?): ValueStringifier {
    return when (value) {
        is Iterable<*> -> IterableStringifier(value)
        else -> AnyStringifier(value)
    }
}

/**
 * A base class which forces subclasses to override their [toString] method.
 */
sealed class ValueStringifier {
    abstract override fun toString(): String
}

/**
 * A default Stringifier that can handle any case.
 */
class AnyStringifier(private val value: Any?) : ValueStringifier() {
    override fun toString() = value?.let { "$it" } ?: "(null)"
}

class IterableStringifier(private val value: Iterable<*>) : ValueStringifier() {
    override fun toString() = "[ ${value.joinToString(", ") { stringifierFor(it).toString() }} ]"
}