package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Summaries

open class ArraySubject<T>(private val actualArray: Array<T>) : IterableSubject<T>(actualArray.toList()) {
    fun isEqualTo(expected: Array<T>) {
        if (!actualArray.contentEquals(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_EQUAL,
                    DetailsFor.expectedActual(expected.contentToString(), actualArray.contentToString())
                )
            )
        }
    }

    fun isNotEqualTo(expected: Array<T>) {
        if (actualArray.contentEquals(expected)) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.expected(actualArray.contentToString())))
        }
    }
}

open class BooleanArraySubject(private val actualArray: BooleanArray) :
    ArraySubject<Boolean>(actualArray.toTypedArray()) {
    private fun BooleanArray.toTypedArray(): Array<Boolean> = this.map { it }.toTypedArray()

    fun isEqualTo(expected: BooleanArray) {
        if (!actualArray.contentEquals(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_EQUAL,
                    DetailsFor.expectedActual(expected.contentToString(), actualArray.contentToString())
                )
            )
        }
    }

    fun isNotEqualTo(expected: BooleanArray) {
        if (actualArray.contentEquals(expected)) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.expected(actualArray.contentToString())))
        }
    }

    fun containsAnyIn(other: BooleanArray): Unit = containsAnyIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsAllIn(other: BooleanArray): OrderedAsserter<Boolean> = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: BooleanArray): Unit = containsNoneIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsExactly(other: BooleanArray): OrderedAsserter<Boolean> = containsExactly(other.toTypedArray())
}

open class ByteArraySubject(private val actualArray: ByteArray) : ArraySubject<Byte>(actualArray.toTypedArray()) {
    private fun ByteArray.toTypedArray(): Array<Byte> = this.map { it }.toTypedArray()

    fun isEqualTo(expected: ByteArray) {
        if (!actualArray.contentEquals(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_EQUAL,
                    DetailsFor.expectedActual(expected.contentToString(), actualArray.contentToString())
                )
            )
        }
    }

    fun isNotEqualTo(expected: ByteArray) {
        if (actualArray.contentEquals(expected)) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.expected(actualArray.contentToString())))
        }
    }

    fun containsAnyIn(other: ByteArray): Unit = containsAnyIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsAllIn(other: ByteArray): OrderedAsserter<Byte> = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: ByteArray): Unit = containsNoneIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsExactly(other: ByteArray): OrderedAsserter<Byte> = containsExactly(other.toTypedArray())
}

open class CharArraySubject(private val actualArray: CharArray) : ArraySubject<Char>(actualArray.toTypedArray()) {
    private fun CharArray.toTypedArray(): Array<Char> = this.map { it }.toTypedArray()

    fun isEqualTo(expected: CharArray) {
        if (!actualArray.contentEquals(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_EQUAL,
                    DetailsFor.expectedActual(expected.contentToString(), actualArray.contentToString())
                )
            )
        }
    }

    fun isNotEqualTo(expected: CharArray) {
        if (actualArray.contentEquals(expected)) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.expected(actualArray.contentToString())))
        }
    }

    fun containsAnyIn(other: CharArray): Unit = containsAnyIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsAllIn(other: CharArray): OrderedAsserter<Char> = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: CharArray): Unit = containsNoneIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsExactly(other: CharArray): OrderedAsserter<Char> = containsExactly(other.toTypedArray())
}

open class ShortArraySubject(private val actualArray: ShortArray) : ArraySubject<Short>(actualArray.toTypedArray()) {
    private fun ShortArray.toTypedArray(): Array<Short> = this.map { it }.toTypedArray()

    fun isEqualTo(expected: ShortArray) {
        if (!actualArray.contentEquals(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_EQUAL,
                    DetailsFor.expectedActual(expected.contentToString(), actualArray.contentToString())
                )
            )
        }
    }

    fun isNotEqualTo(expected: ShortArray) {
        if (actualArray.contentEquals(expected)) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.expected(actualArray.contentToString())))
        }
    }

    fun containsAnyIn(other: ShortArray): Unit = containsAnyIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsAllIn(other: ShortArray): OrderedAsserter<Short> = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: ShortArray): Unit = containsNoneIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsExactly(other: ShortArray): OrderedAsserter<Short> = containsExactly(other.toTypedArray())
}

open class IntArraySubject(private val actualArray: IntArray) : ArraySubject<Int>(actualArray.toTypedArray()) {
    private fun IntArray.toTypedArray(): Array<Int> = this.map { it }.toTypedArray()

    fun isEqualTo(expected: IntArray) {
        if (!actualArray.contentEquals(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_EQUAL,
                    DetailsFor.expectedActual(expected.contentToString(), actualArray.contentToString())
                )
            )
        }
    }

    fun isNotEqualTo(expected: IntArray) {
        if (actualArray.contentEquals(expected)) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.expected(actualArray.contentToString())))
        }
    }

    fun containsAnyIn(other: IntArray): Unit = containsAnyIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsAllIn(other: IntArray): OrderedAsserter<Int> = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: IntArray): Unit = containsNoneIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsExactly(other: IntArray): OrderedAsserter<Int> = containsExactly(other.toTypedArray())
}

open class LongArraySubject(private val actualArray: LongArray) : ArraySubject<Long>(actualArray.toTypedArray()) {
    private fun LongArray.toTypedArray(): Array<Long> = this.map { it }.toTypedArray()

    fun isEqualTo(expected: LongArray) {
        if (!actualArray.contentEquals(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_EQUAL,
                    DetailsFor.expectedActual(expected.contentToString(), actualArray.contentToString())
                )
            )
        }
    }

    fun isNotEqualTo(expected: LongArray) {
        if (actualArray.contentEquals(expected)) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.expected(actualArray.contentToString())))
        }
    }

    fun containsAnyIn(other: LongArray): Unit = containsAnyIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsAllIn(other: LongArray): OrderedAsserter<Long> = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: LongArray): Unit = containsNoneIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsExactly(other: LongArray): OrderedAsserter<Long> = containsExactly(other.toTypedArray())
}

open class FloatArraySubject(private val actualArray: FloatArray) : ArraySubject<Float>(actualArray.toTypedArray()) {
    private fun FloatArray.toTypedArray(): Array<Float> = this.map { it }.toTypedArray()

    fun isEqualTo(expected: FloatArray) {
        if (!actualArray.contentEquals(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_EQUAL,
                    DetailsFor.expectedActual(expected.contentToString(), actualArray.contentToString())
                )
            )
        }
    }

    fun isNotEqualTo(expected: FloatArray) {
        if (actualArray.contentEquals(expected)) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.expected(actualArray.contentToString())))
        }
    }

    fun containsAnyIn(other: FloatArray): Unit = containsAnyIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsAllIn(other: FloatArray): OrderedAsserter<Float> = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: FloatArray): Unit = containsNoneIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsExactly(other: FloatArray): OrderedAsserter<Float> = containsExactly(other.toTypedArray())
}

open class DoubleArraySubject(private val actualArray: DoubleArray) : ArraySubject<Double>(actualArray.toTypedArray()) {
    private fun DoubleArray.toTypedArray(): Array<Double> = this.map { it }.toTypedArray()

    fun isEqualTo(expected: DoubleArray) {
        if (!actualArray.contentEquals(expected)) {
            report(
                Report(
                    Summaries.EXPECTED_EQUAL,
                    DetailsFor.expectedActual(expected.contentToString(), actualArray.contentToString())
                )
            )
        }
    }

    fun isNotEqualTo(expected: DoubleArray) {
        if (actualArray.contentEquals(expected)) {
            report(Report(Summaries.EXPECTED_NOT_EQUAL, DetailsFor.expected(actualArray.contentToString())))
        }
    }

    fun containsAnyIn(other: DoubleArray): Unit = containsAnyIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsAllIn(other: DoubleArray): OrderedAsserter<Double> = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: DoubleArray): Unit = containsNoneIn(other.toTypedArray())
    @IgnorableReturnValue
    fun containsExactly(other: DoubleArray): OrderedAsserter<Double> = containsExactly(other.toTypedArray())
}
