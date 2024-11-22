package com.varabyte.truthish

import com.varabyte.truthish.failure.*
import com.varabyte.truthish.subjects.*

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
fun <T> assertThat(actual: Array<T>) = ArraySubject(actual)
fun assertThat(actual: BooleanArray) = BooleanArraySubject(actual)
fun assertThat(actual: ByteArray) = ByteArraySubject(actual)
fun assertThat(actual: CharArray) = CharArraySubject(actual)
fun assertThat(actual: ShortArray) = ShortArraySubject(actual)
fun assertThat(actual: IntArray) = IntArraySubject(actual)
fun assertThat(actual: LongArray) = LongArraySubject(actual)
fun assertThat(actual: FloatArray) = FloatArraySubject(actual)
fun assertThat(actual: DoubleArray) = DoubleArraySubject(actual)
// Adding a new [assertThat] here? Also add it to SummarizedSubjectBuilder and AssetAllScope

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
class AssertAllScope(summary: String?) {
    internal val deferredStrategy = DeferredStrategy(summary)

    fun that(actual: Any?) = NullableSubject(actual).withStrategy(deferredStrategy)
    fun that(actual: Any) = NotNullSubject(actual).withStrategy(deferredStrategy)
    fun <T: Comparable<T>> that(actual: T) = ComparableSubject(actual).withStrategy(deferredStrategy)
    fun that(actual: Boolean) = BooleanSubject(actual).withStrategy(deferredStrategy)
    fun that(actual: Byte) = ByteSubject(actual).withStrategy(deferredStrategy)
    fun that(actual: Short) = ShortSubject(actual).withStrategy(deferredStrategy)
    fun that(actual: Int) = IntSubject(actual).withStrategy(deferredStrategy)
    fun that(actual: Long) = LongSubject(actual).withStrategy(deferredStrategy)
    fun that(actual: Float) = FloatSubject(actual).withStrategy(deferredStrategy)
    fun that(actual: Double) = DoubleSubject(actual).withStrategy(deferredStrategy)
    fun that(actual: String) = StringSubject(actual).withStrategy(deferredStrategy)
    fun <T, I: Iterable<T>> that(actual: I) = IterableSubject(actual).withStrategy(deferredStrategy)
    fun <K, V, T: Map<K, V>> that(actual: T) = MapSubject(actual).withStrategy(deferredStrategy)
    fun <T, S: Sequence<T>> that(actual: S) = IterableSubject(actual.asIterable()).withStrategy(deferredStrategy)
    fun <T> that(actual: Array<T>) = ArraySubject(actual).withStrategy(deferredStrategy)
    fun that(actual: BooleanArray) = BooleanArraySubject(actual).withStrategy(deferredStrategy)
    fun that(actual: ByteArray) = ByteArraySubject(actual).withStrategy(deferredStrategy)
    fun that(actual: CharArray) = CharArraySubject(actual).withStrategy(deferredStrategy)
    fun that(actual: ShortArray) = ShortArraySubject(actual).withStrategy(deferredStrategy)
    fun that(actual: IntArray) = IntArraySubject(actual).withStrategy(deferredStrategy)
    fun that(actual: LongArray) = LongArraySubject(actual).withStrategy(deferredStrategy)
    fun that(actual: FloatArray) = FloatArraySubject(actual).withStrategy(deferredStrategy)
    fun that(actual: DoubleArray) = DoubleArraySubject(actual).withStrategy(deferredStrategy)

    fun withMessage(message: String) = SummarizedSubjectBuilder(message, deferredStrategy)
}


fun assertWithMessage(message: String) = SummarizedSubjectBuilder(message)
class SummarizedSubjectBuilder(private val message: String, private val strategyOverride: FailureStrategy? = null) {
    private inline fun <reified R: Reportable> R.withStrategyOverride() = if (strategyOverride != null) withStrategy(strategyOverride) else this

    fun that(actual: Any?) = NullableSubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: Any) = NotNullSubject(actual).withMessage(message).withStrategyOverride()
    fun <T: Comparable<T>> that(actual: T) = ComparableSubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: Boolean) = BooleanSubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: Byte) = ByteSubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: Short) = ShortSubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: Int) = IntSubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: Long) = LongSubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: Float) = FloatSubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: Double) = DoubleSubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: String) = StringSubject(actual).withMessage(message).withStrategyOverride()
    fun <T, I: Iterable<T>> that(actual: I) = IterableSubject(actual).withMessage(message).withStrategyOverride()
    fun <K, V, T: Map<K, V>> that(actual: T) = MapSubject(actual).withMessage(message).withStrategyOverride()
    fun <T, S: Sequence<T>> that(actual: S) = IterableSubject(actual.asIterable()).withMessage(message).withStrategyOverride()
    fun <T> that(actual: Array<T>) = ArraySubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: BooleanArray) = BooleanArraySubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: ByteArray) = ByteArraySubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: CharArray) = CharArraySubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: ShortArray) = ShortArraySubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: IntArray) = IntArraySubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: LongArray) = LongArraySubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: FloatArray) = FloatArraySubject(actual).withMessage(message).withStrategyOverride()
    fun that(actual: DoubleArray) = DoubleArraySubject(actual).withMessage(message).withStrategyOverride()
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
