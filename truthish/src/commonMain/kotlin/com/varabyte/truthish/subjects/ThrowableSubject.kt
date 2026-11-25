package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.SkipStrategy
import com.varabyte.truthish.failure.derivedFrom
import com.varabyte.truthish.failure.valueOf
import com.varabyte.truthish.failure.withStrategy

/**
 * A subject for a throwable, which can also check its message and cause.
 *
 * For example:
 * ```
 * val e = assertThrows<IllegalStateException> { parse("") }
 * assertThat(e).hasMessageThat().contains("empty")
 * assertThat(e).hasCauseThat().isInstanceOf<IOException>()
 * ```
 */
class ThrowableSubject(private val actual: Throwable) : NotNullSubject<Throwable>(actual) {
    /**
     * Returns a subject for this throwable's message, reporting a failure if it has none.
     *
     * To check that a throwable has no message, use `assertThat(e.message).isNull()`.
     */
    fun hasMessageThat(): StringSubject {
        val path = "${findPath()}.message"
        val message = actual.message
        if (message == null) {
            NullableSubject(message).derivedFrom(this).valueOf(path).isNotNull()
            return StringSubject("").withStrategy(SkipStrategy)
        }
        return StringSubject(message).derivedFrom(this).valueOf(path)
    }

    /**
     * Returns a subject for this throwable's cause, reporting a failure if it has none.
     *
     * To check that a throwable has no cause, use `assertThat(e.cause).isNull()`.
     */
    fun hasCauseThat(): ThrowableSubject {
        val path = "${findPath()}.cause"
        val cause = actual.cause
        if (cause == null) {
            NullableSubject(cause).derivedFrom(this).valueOf(path).isNotNull()
            return ThrowableSubject(actual).withStrategy(SkipStrategy)
        }
        return ThrowableSubject(cause).derivedFrom(this).valueOf(path)
    }

    /** The start of the [valueOf] paths for this throwable's message and cause, e.g. `ParseException.cause`. */
    private fun findPath(): String {
        return valueOf ?: run {
            // An anonymous class has no simple name, or (e.g. on Wasm) a special name like `<no name provided>`. Kotlin
            // wraps special names in angle brackets, which a real class name can't contain.
            val className = actual::class.simpleName?.takeUnless { it.startsWith('<') }
            className ?: "Throwable"
        }
    }
}
