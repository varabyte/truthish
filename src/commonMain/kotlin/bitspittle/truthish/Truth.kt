package bitspittle.truthish

import bitspittle.truthish.failure.DetailsFor
import bitspittle.truthish.failure.Report
import bitspittle.truthish.failure.Summaries
import bitspittle.truthish.failure.withMessage
import bitspittle.truthish.subjects.BooleanSubject
import bitspittle.truthish.subjects.NotNullSubject
import bitspittle.truthish.subjects.NullableSubject

fun assertThat(actual: Any?) = NullableSubject(actual)
fun assertThat(actual: Any) = NotNullSubject(actual)
fun assertThat(actual: Boolean) = BooleanSubject(actual)
// Adding a new [assertThat] here? Also add it to SummarizedSubjectBuilder

fun assertWithMessage(message: String) = SummarizedSubjectBuilder(message)
class SummarizedSubjectBuilder(private val message: String) {
    fun that(actual: Any?) = NullableSubject(actual).withMessage(message)
    fun that(actual: Any) = NotNullSubject(actual).withMessage(message)
    fun that(actual: Boolean) = BooleanSubject(actual).withMessage(message)
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