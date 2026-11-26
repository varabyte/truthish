package com.varabyte.truthish

import com.varabyte.truthish.failure.*
import com.varabyte.truthish.subjects.*

fun assertThat(actual: Any?) = AssertionEntryPoint.that(actual)
fun assertThat(actual: Any) = AssertionEntryPoint.that(actual)
fun <T: Comparable<T>> assertThat(actual: T) = AssertionEntryPoint.that(actual)
fun assertThat(actual: Boolean) = AssertionEntryPoint.that(actual)
fun assertThat(actual: Byte) = AssertionEntryPoint.that(actual)
fun assertThat(actual: Short) = AssertionEntryPoint.that(actual)
fun assertThat(actual: Int) = AssertionEntryPoint.that(actual)
fun assertThat(actual: Long) = AssertionEntryPoint.that(actual)
fun assertThat(actual: Float) = AssertionEntryPoint.that(actual)
fun assertThat(actual: Double) = AssertionEntryPoint.that(actual)
fun assertThat(actual: String) = AssertionEntryPoint.that(actual)
fun assertThat(actual: Throwable) = AssertionEntryPoint.that(actual)
fun <T, I: Iterable<T>> assertThat(actual: I) = AssertionEntryPoint.that(actual)
fun <K, V, T: Map<K, V>> assertThat(actual: T) = AssertionEntryPoint.that(actual)
fun <T, S: Sequence<T>> assertThat(actual: S) = AssertionEntryPoint.that(actual)
fun <T> assertThat(actual: Array<T>) = AssertionEntryPoint.that(actual)
fun assertThat(actual: BooleanArray) = AssertionEntryPoint.that(actual)
fun assertThat(actual: ByteArray) = AssertionEntryPoint.that(actual)
fun assertThat(actual: CharArray) = AssertionEntryPoint.that(actual)
fun assertThat(actual: ShortArray) = AssertionEntryPoint.that(actual)
fun assertThat(actual: IntArray) = AssertionEntryPoint.that(actual)
fun assertThat(actual: LongArray) = AssertionEntryPoint.that(actual)
fun assertThat(actual: FloatArray) = AssertionEntryPoint.that(actual)
fun assertThat(actual: DoubleArray) = AssertionEntryPoint.that(actual)
// Keep in sync with AssertionEntryPoint.kt. Validated by EntryPointsKeptInSyncTest.

/**
 * Create a block of grouped assertions.
 *
 * Within an `assertAll` block, all assertions are run, and if any fail, all failures are deferred to the end of the
 * block and reported all at once.
 *
 * For example, a test like:
 * ```
 * val person = personDatabase.query(id = 123)
 * assertAll {
 *   that(person.name).isEqualTo("Alice")
 *   that(person.age).isEqualTo(30)
 *   that(person.id).isEqualTo(123)
 * }
 * ```
 *
 * could output something like:
 *
 * ```
 * Grouped assertions had 2 failure(s)
 *
 * Failure 1:
 * Two objects were not equal
 *
 * Expected: "Bob"
 * But was : "Alice"
 * At      : "org.example.DatabaseTest$queryPerson$1.invoke(DatabaseTest.kt:47)"
 *
 * Failure 2:
 * Two objects were not equal
 *
 * Expected: 45
 * But was : 30
 * At      : "org.example.DatabaseTest$queryPerson$1.invoke(DatabaseTest.kt:48)"
 * ```
 */
fun assertAll(summary: String? = null, block: AssertAllScope.() -> Unit) {
    val assertAllScope = AssertAllScope(summary)
    assertAllScope.block()
    assertAllScope.deferredStrategy.handleNow()
}
class AssertAllScope(summary: String?) : AssertionEntryPoint {
    internal val deferredStrategy = DeferredStrategy(summary)

    override fun <S : Reportable> prepare(subject: S): S {
        subject.strategy = deferredStrategy
        return subject
    }

    fun withMessage(message: String) = MessageEntryPoint(message, deferredStrategy)
}

/**
 * Create an assertion with a message that describes it, which can be useful for providing more context to a failure.
 *
 * For example:
 *
 * ```
 * assertWithMessage("IDs should match").that(item1.id).isEqualTo(item2.id)
 * ```
 *
 * could generate output like:
 *
 * ```
 * Two objects were not equal
 *
 * Message : IDs should match
 * Expected: 672528
 * But was : 187974
 * ```
 *
 * Without that message, you might just see a test failure that showed two random numbers not being the same, which
 * would require the developer to dig into the test to understand what was being compared.
 *
 * Note that the example above is functionally identical to:
 *
 * ```
 * assertThat(item1.id).withMessage("IDs should match").isEqualTo(item2.id)
 * ```
 *
 * but the `assertWithMessage` approach is provided as it reads a lot more naturally and doesn't obscure the check by
 * jamming a long message in the middle of the assertion. In fact, despite being syntax sugar, you should prefer
 * using `assertWithMessage` over `withMessage` for this reason.
 */
fun assertWithMessage(message: String) = MessageEntryPoint(message)
class MessageEntryPoint(
    private val message: String,
    private val strategyOverride: FailureStrategy? = null,
) : AssertionEntryPoint {
    override fun <S : Reportable> prepare(subject: S): S {
        subject.message = message
        strategyOverride?.let { subject.strategy = it }
        return subject
    }
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
// NOTE: `block` returns `Any?` and not `Unit` so that it will consume any intermediate value generated by any code it
// calls. This is useful because when a user calls a method expecting it to throw as a side effect, they often won't
// care about the resulting value because it will never actually get returned before crashing. Starting in Kotlin 2.3.0,
// users can start opting into warnings for code that doesn't use returned values, which would require ugly `val _ = `
// code (such as `assertThrows { val _ = explode() }`) to suppress it.
// See also: https://github.com/varabyte/truthish/issues/13
inline fun <reified T : Throwable> assertThrows(message: String? = null, block: () -> Any?): T {
    val unexpectedException = try {
        block()
        null
    }
    catch (t: Throwable) {
        if (t is T) return t
        t
    }

    val (summary, details) = if (unexpectedException == null) {
        Summaries.EXPECTED_EXCEPTION to DetailsFor.expected(T::class)
    }
    else {
        Summaries.EXPECTED_EXCEPTION_TYPE to DetailsFor.expectedActual(T::class, unexpectedException::class)
    }
    if (message != null) details.addMessage(message)
    val report = Report(summary, details)

    // Keep the unexpected exception as the cause, since its message and stack trace are usually what's needed to debug
    throw AssertionWithCauseError(report.toString(), cause = unexpectedException)
}
