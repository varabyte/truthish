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

    fun containsAnyIn(other: BooleanArray) = containsAnyIn(other.toTypedArray())
    fun containsAllIn(other: BooleanArray) = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: BooleanArray) = containsNoneIn(other.toTypedArray())
    fun containsExactly(other: BooleanArray) = containsExactly(other.toTypedArray())
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

    fun containsAnyIn(other: ByteArray) = containsAnyIn(other.toTypedArray())
    fun containsAllIn(other: ByteArray) = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: ByteArray) = containsNoneIn(other.toTypedArray())
    fun containsExactly(other: ByteArray) = containsExactly(other.toTypedArray())
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

    fun containsAnyIn(other: CharArray) = containsAnyIn(other.toTypedArray())
    fun containsAllIn(other: CharArray) = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: CharArray) = containsNoneIn(other.toTypedArray())
    fun containsExactly(other: CharArray) = containsExactly(other.toTypedArray())
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

    fun containsAnyIn(other: ShortArray) = containsAnyIn(other.toTypedArray())
    fun containsAllIn(other: ShortArray) = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: ShortArray) = containsNoneIn(other.toTypedArray())
    fun containsExactly(other: ShortArray) = containsExactly(other.toTypedArray())
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

    fun containsAnyIn(other: IntArray) = containsAnyIn(other.toTypedArray())
    fun containsAllIn(other: IntArray) = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: IntArray) = containsNoneIn(other.toTypedArray())
    fun containsExactly(other: IntArray) = containsExactly(other.toTypedArray())
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

    fun containsAnyIn(other: LongArray) = containsAnyIn(other.toTypedArray())
    fun containsAllIn(other: LongArray) = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: LongArray) = containsNoneIn(other.toTypedArray())
    fun containsExactly(other: LongArray) = containsExactly(other.toTypedArray())
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

    fun containsAnyIn(other: FloatArray) = containsAnyIn(other.toTypedArray())
    fun containsAllIn(other: FloatArray) = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: FloatArray) = containsNoneIn(other.toTypedArray())
    fun containsExactly(other: FloatArray) = containsExactly(other.toTypedArray())
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

    fun containsAnyIn(other: DoubleArray) = containsAnyIn(other.toTypedArray())
    fun containsAllIn(other: DoubleArray) = containsAllIn(other.toTypedArray())
    fun containsNoneIn(other: DoubleArray) = containsNoneIn(other.toTypedArray())
    fun containsExactly(other: DoubleArray) = containsExactly(other.toTypedArray())
}
