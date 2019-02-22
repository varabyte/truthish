package bitspittle.truthish

import bitspittle.truthish.failure.DetailsFor
import bitspittle.truthish.failure.Report
import bitspittle.truthish.failure.Summaries
import bitspittle.truthish.failure.withMessage
import bitspittle.truthish.subjects.BooleanSubject
import bitspittle.truthish.subjects.ByteSubject
import bitspittle.truthish.subjects.ComparableSubject
import bitspittle.truthish.subjects.DoubleSubject
import bitspittle.truthish.subjects.FloatSubject
import bitspittle.truthish.subjects.IntSubject
import bitspittle.truthish.subjects.IterableSubject
import bitspittle.truthish.subjects.LongSubject
import bitspittle.truthish.subjects.NotNullSubject
import bitspittle.truthish.subjects.NullableSubject
import bitspittle.truthish.subjects.ShortSubject
import bitspittle.truthish.subjects.StringSubject

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
}

/**
 * Helpful utility function for verifying that a block throws an expected exception type.
 * This method also returns the exception, so further asserts can be made against it if desired.
 *
 * Unfortunately, there is no way to override the failure strategy for this assert, since
 * for usability we need to guarantee that we'll either return a valid exception or abort via a
 * different exception, so we throw [AssertionError] directly.
 */
inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T {
    val report = try {
        block()
        Report(Summaries.EXPECTED_EXCEPTION, DetailsFor.expected(T::class))
    }
    catch (t: Throwable) {
        if (t !is T) {
            Report(Summaries.EXPECTED_EXCEPTION, DetailsFor.expectedActual(T::class, t::class))
        }
        else {
            return t
        }
    }

    throw AssertionError(report)
}