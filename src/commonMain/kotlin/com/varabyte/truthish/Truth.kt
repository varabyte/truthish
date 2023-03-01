package com.varabyte.truthish

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.withMessage
import com.varabyte.truthish.subjects.BooleanSubject
import com.varabyte.truthish.subjects.ByteSubject
import com.varabyte.truthish.subjects.ComparableSubject
import com.varabyte.truthish.subjects.DoubleSubject
import com.varabyte.truthish.subjects.FloatSubject
import com.varabyte.truthish.subjects.IntSubject
import com.varabyte.truthish.subjects.IterableSubject
import com.varabyte.truthish.subjects.LongSubject
import com.varabyte.truthish.subjects.MapSubject
import com.varabyte.truthish.subjects.NotNullSubject
import com.varabyte.truthish.subjects.NullableSubject
import com.varabyte.truthish.subjects.ShortSubject
import com.varabyte.truthish.subjects.StringSubject

fun assertThat(actual: Any?) = NullableSubject(actual)
fun assertThat(actual: Any) = NotNullSubject(actual)
fun <T: Comparable<T>> assertThat(actual: T) = ComparableSubject(actual)
fun assertThat(actual: Boolean) = BooleanSubject(actual)
fun assertThat(actual: Byte) = ByteSubject(actual)
fun assertThat(actual: Short) = ShortSubject(actual)
fun assertThat(actual: Int) = IntSubject(actual)
fun assertThat(actual: Long) = LongSubject(actual)
fun assertThat(actual: Float) = FloatSubject(actual)
fun assertThat(actual: Double) = DoubleSubject(actual)
fun assertThat(actual: String) = StringSubject(actual)
fun <T, I: Iterable<T>> assertThat(actual: I) = IterableSubject(actual)
fun <K, V, T: Map<K, V>> assertThat(actual: T) = MapSubject(actual)
fun <T, S: Sequence<T>> assertThat(actual: S) = IterableSubject(actual.asIterable())
// Adding a new [assertThat] here? Also add it to SummarizedSubjectBuilder

fun assertWithMessage(message: String) = SummarizedSubjectBuilder(message)
class SummarizedSubjectBuilder(private val message: String) {
    fun that(actual: Any?) = NullableSubject(actual).withMessage(message)
    fun that(actual: Any) = NotNullSubject(actual).withMessage(message)
    fun <T: Comparable<T>> that(actual: T) = ComparableSubject(actual).withMessage(message)
    fun that(actual: Boolean) = BooleanSubject(actual).withMessage(message)
    fun that(actual: Byte) = ByteSubject(actual).withMessage(message)
    fun that(actual: Short) = ShortSubject(actual).withMessage(message)
    fun that(actual: Int) = IntSubject(actual).withMessage(message)
    fun that(actual: Long) = LongSubject(actual).withMessage(message)
    fun that(actual: Float) = FloatSubject(actual).withMessage(message)
    fun that(actual: Double) = DoubleSubject(actual).withMessage(message)
    fun that(actual: String) = StringSubject(actual).withMessage(message)
    fun <T, I: Iterable<T>> that(actual: I) = IterableSubject(actual).withMessage(message)
    fun <K, V, T: Map<K, V>> that(actual: T) = MapSubject(actual).withMessage(message)
    fun <T, S: Sequence<T>> that(actual: S) = IterableSubject(actual.asIterable()).withMessage(message)
}

/**
 * Helpful utility function for verifying that a block throws an expected exception type.
 * This method also returns the exception, so further asserts can be made against it if desired.
 *
 * Unfortunately, there is no way to override the failure strategy for this assert, since
 * for usability we need to guarantee that we'll either return a valid exception or abort via a
 * different exception, so we throw [AssertionError] directly.
 *
 * @param message If set, include a custom message in the final assertion.
 */
inline fun <reified T : Throwable> assertThrows(message: String? = null, block: () -> Unit): T {
    val report = try {
        block()
        Report(Summaries.EXPECTED_EXCEPTION, DetailsFor.expected(T::class).apply { if (message != null) add("Message" to message) })
    }
    catch (t: Throwable) {
        if (t !is T) {
            Report(Summaries.EXPECTED_EXCEPTION, DetailsFor.expectedActual(T::class, t::class).apply { if (message != null) add("Message" to message) })
        }
        else {
            return t
        }
    }

    throw AssertionError(report)
}