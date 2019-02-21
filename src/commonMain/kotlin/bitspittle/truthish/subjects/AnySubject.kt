package bitspittle.truthish.subjects

import bitspittle.truthish.failure.DetailsFor
import bitspittle.truthish.failure.Report
import bitspittle.truthish.failure.Reportable
import bitspittle.truthish.failure.Summaries
import kotlin.reflect.KClass

/**
 * Base-class for a subject with an [Any] value that one would want to test against another.
 *
 * Code cannot instantiate this base-class directly. Instead, it must instantiate a
 * [NullableSubject] or [NotNullSubject].
 */
abstract class AnySubject<T: Any>(private val actual: T?) : Reportable() {
    fun isEqualTo(expected: Any?) {
        if (actual != expected) {
            report(Report(Summaries.EXPECTED_EQUAL, DetailsFor.expectedActual(expected, actual)))
        }
    }

    fun isNotEqualTo(expected: Any?) {
        if (actual == expected) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.actual(actual)))
        }
    }

    fun isSameAs(expected: Any?) {
        if (actual !== expected) {
            report(Report(Summaries.EXPECTED_SAME, DetailsFor.expectedActual(expected, actual)))
        }
    }

    fun isNotSameAs(expected: Any?) {
        if (actual === expected) {
            report(Report(Summaries.EXPECTED_NOT_SAME, DetailsFor.actual(actual)))
        }
    }

    fun isInstanceOf(expected: KClass<*>) {
        if (!expected.isInstance(actual)) {
            val kClass = actual?.let { it::class }
            report(Report(Summaries.EXPECTED_INSTANCE, DetailsFor.expectedActual(expected, kClass)))

        }
    }

    fun isNotInstanceOf(expected: KClass<*>) {
        if (expected.isInstance(actual)) {
            val kClass = actual!!::class
            report(Report(Summaries.EXPECTED_NOT_INSTANCE, DetailsFor.actual(kClass)))

        }
    }

    inline fun <reified T: Any> isInstanceOf() = isInstanceOf(T::class)
    inline fun <reified T: Any> isNotInstanceOf() = isNotInstanceOf(T::class)
}

/**
 * A subject which enforces a non-null value.
 *
 * This allows excluding assertion tests that have to do with nullity checks.
 */
open class NotNullSubject<T: Any>(private val actual: T) : AnySubject<T>(actual)

/**
 * A subject whose value can be null or non-null.
 */
open class NullableSubject<T: Any>(private val actual: T?) : AnySubject<T>(actual) {
    /**
     * Verify that this subject's value is actually null.
     */
    fun isNull() {
        if (actual != null) {
            report(Report(Summaries.EXPECTED_NULL, DetailsFor.actual(actual)))
        }
    }

    /**
     * Verify that this subject's value is not null.
     *
     * Note that the more idiomatic way to do this is by using Kotlin's !! operator, but this
     * method is still provided for completion / readability.
     */
    fun isNotNull() {
        if (actual == null) {
            report(Report(Summaries.EXPECTED_NOT_NULL))
        }
    }
}