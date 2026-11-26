package com.varabyte.truthish

import com.varabyte.truthish.failure.Reportable
import com.varabyte.truthish.subjects.*

/**
 * Where an assertion chain starts, via `that(...)`.
 *
 * This interface is shared by every different system for beginning a fluent assertion chain, e.g. all calls to
 * [assertThat], inside [assertAll], or after [assertWithMessage].
 *
 * Each entry point prepares its subjects with some shared setup, like a deferred failure strategy or a message, via
 * [prepare].
 *
 * To add support for a custom subject, write a `that` extension that passes it to [prepare]:
 * ```
 * fun AssertionEntryPoint.that(person: Person) = prepare(PersonSubject(person))
 * ```
 *
 * Then add a top-level `assertThat` that uses the [Default] entry point:
 * ```
 * fun assertThat(person: Person) = AssertionEntryPoint.that(person)
 * ```
 *
 * Your subject then works everywhere the built-in ones do:
 * ```
 * assertThat(alice).hasName("Alice")
 * assertAll {
 *     that(bob).hasName("Bob")
 *     that(bob.age).isAtLeast(18)
 * }
 * assertWithMessage("This user was renamed").that(charles).hasName("Charlie")
 * ```
 */
interface AssertionEntryPoint {
    /** The entry point behind the top-level `assertThat` functions. */
    companion object Default : AssertionEntryPoint {
        override fun <S : Reportable> prepare(subject: S) = subject
    }

    /** Applies this entry point's setup to [subject] and returns it. */
    fun <S : Reportable> prepare(subject: S): S
}

fun AssertionEntryPoint.that(actual: Any?) = prepare(NullableSubject(actual))
fun AssertionEntryPoint.that(actual: Any) = prepare(NotNullSubject(actual))
fun <T: Comparable<T>> AssertionEntryPoint.that(actual: T) = prepare(ComparableSubject(actual))
fun AssertionEntryPoint.that(actual: Boolean) = prepare(BooleanSubject(actual))
fun AssertionEntryPoint.that(actual: Byte) = prepare(ByteSubject(actual))
fun AssertionEntryPoint.that(actual: Short) = prepare(ShortSubject(actual))
fun AssertionEntryPoint.that(actual: Int) = prepare(IntSubject(actual))
fun AssertionEntryPoint.that(actual: Long) = prepare(LongSubject(actual))
fun AssertionEntryPoint.that(actual: Float) = prepare(FloatSubject(actual))
fun AssertionEntryPoint.that(actual: Double) = prepare(DoubleSubject(actual))
fun AssertionEntryPoint.that(actual: String) = prepare(StringSubject(actual))
fun AssertionEntryPoint.that(actual: Throwable) = prepare(ThrowableSubject(actual))
fun <T, I: Iterable<T>> AssertionEntryPoint.that(actual: I) = prepare(IterableSubject(actual))
fun <K, V, T: Map<K, V>> AssertionEntryPoint.that(actual: T) = prepare(MapSubject(actual))
fun <T, S: Sequence<T>> AssertionEntryPoint.that(actual: S) = prepare(IterableSubject(actual.asIterable()))
fun <T> AssertionEntryPoint.that(actual: Array<T>) = prepare(ArraySubject(actual))
fun AssertionEntryPoint.that(actual: BooleanArray) = prepare(BooleanArraySubject(actual))
fun AssertionEntryPoint.that(actual: ByteArray) = prepare(ByteArraySubject(actual))
fun AssertionEntryPoint.that(actual: CharArray) = prepare(CharArraySubject(actual))
fun AssertionEntryPoint.that(actual: ShortArray) = prepare(ShortArraySubject(actual))
fun AssertionEntryPoint.that(actual: IntArray) = prepare(IntArraySubject(actual))
fun AssertionEntryPoint.that(actual: LongArray) = prepare(LongArraySubject(actual))
fun AssertionEntryPoint.that(actual: FloatArray) = prepare(FloatArraySubject(actual))
fun AssertionEntryPoint.that(actual: DoubleArray) = prepare(DoubleArraySubject(actual))
// Keep in sync with Truth.kt. Validated by EntryPointsKeptInSyncTest.
