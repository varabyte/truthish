package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.derivedFrom
import kotlin.math.absoluteValue

class ByteSubject(private val actual: Byte) : ComparableSubject<Byte>(actual) {
    // bytes do not have their own range type (`1.toByte()..10.toByte()` is an `IntRange`), so check as an int
    fun isIn(range: IntRange) = IntSubject(actual.toInt()).derivedFrom(this).isIn(range)
    fun isNotIn(range: IntRange) = IntSubject(actual.toInt()).derivedFrom(this).isNotIn(range)
}

class ShortSubject(private val actual: Short) : ComparableSubject<Short>(actual) {
    // shorts do not have their own range type (`1.toShort()..10.toShort()` is an `IntRange`), so check as an int
    fun isIn(range: IntRange) = IntSubject(actual.toInt()).derivedFrom(this).isIn(range)
    fun isNotIn(range: IntRange) = IntSubject(actual.toInt()).derivedFrom(this).isNotIn(range)
}

class IntSubject(actual: Int) : ComparableSubject<Int>(actual)
class LongSubject(actual: Long) : ComparableSubject<Long>(actual)

class FloatSubject(internal val actual: Float) : ComparableSubject<Float>(actual) {
    fun isWithin(epsilon: Float) = FloatEpsilonAsserter(this, epsilon)
    fun isNotWithin(epsilon: Float) = FloatEpsilonAsserter(this, epsilon, within = false)

    fun isNaN() {
        if (!actual.isNaN()) report(Report(Summaries.EXPECTED_NAN, DetailsFor.actual(actual)))
    }

    fun isNotNaN() {
        if (actual.isNaN()) report(Report(Summaries.EXPECTED_NOT_NAN))
    }

    /** Checks that this value is neither infinite nor NaN. */
    fun isFinite() {
        if (!actual.isFinite()) report(Report(Summaries.EXPECTED_FINITE, DetailsFor.actual(actual)))
    }

    fun isPositiveInfinity() {
        if (actual != Float.POSITIVE_INFINITY) {
            report(Report(Summaries.EXPECTED_POSITIVE_INFINITY, DetailsFor.actual(actual)))
        }
    }

    fun isNegativeInfinity() {
        if (actual != Float.NEGATIVE_INFINITY) {
            report(Report(Summaries.EXPECTED_NEGATIVE_INFINITY, DetailsFor.actual(actual)))
        }
    }

    /**
     * Checks that this value is zero, where `-0f` counts as zero too.
     *
     * This differs from `isEqualTo(0f)`, which tells `0f` and `-0f` apart.
     */
    fun isZero() {
        if (actual != 0f) report(Report(Summaries.EXPECTED_ZERO, DetailsFor.actual(actual)))
    }

    /** Checks that this value isn't zero, where `-0f` counts as zero too. NaN isn't zero, so it passes. */
    fun isNonZero() {
        if (actual == 0f) report(Report(Summaries.EXPECTED_NON_ZERO, DetailsFor.actual(actual)))
    }
}

class DoubleSubject(internal val actual: Double) : ComparableSubject<Double>(actual) {
    fun isWithin(epsilon: Double) = DoubleEpsilonAsserter(this, epsilon)
    fun isNotWithin(epsilon: Double) = DoubleEpsilonAsserter(this, epsilon, within = false)

    fun isNaN() {
        if (!actual.isNaN()) report(Report(Summaries.EXPECTED_NAN, DetailsFor.actual(actual)))
    }

    fun isNotNaN() {
        if (actual.isNaN()) report(Report(Summaries.EXPECTED_NOT_NAN))
    }

    /** Checks that this value is neither infinite nor NaN. */
    fun isFinite() {
        if (!actual.isFinite()) report(Report(Summaries.EXPECTED_FINITE, DetailsFor.actual(actual)))
    }

    fun isPositiveInfinity() {
        if (actual != Double.POSITIVE_INFINITY) {
            report(Report(Summaries.EXPECTED_POSITIVE_INFINITY, DetailsFor.actual(actual)))
        }
    }

    fun isNegativeInfinity() {
        if (actual != Double.NEGATIVE_INFINITY) {
            report(Report(Summaries.EXPECTED_NEGATIVE_INFINITY, DetailsFor.actual(actual)))
        }
    }

    /**
     * Checks that this value is zero, where `-0.0` counts as zero too.
     *
     * This differs from `isEqualTo(0.0)`, which tells `0.0` and `-0.0` apart.
     */
    fun isZero() {
        if (actual != 0.0) report(Report(Summaries.EXPECTED_ZERO, DetailsFor.actual(actual)))
    }

    /** Checks that this value isn't zero, where `-0.0` counts as zero too. NaN isn't zero, so it passes. */
    fun isNonZero() {
        if (actual == 0.0) report(Report(Summaries.EXPECTED_NON_ZERO, DetailsFor.actual(actual)))
    }
}

class FloatEpsilonAsserter(
    private val parent: FloatSubject,
    private val epsilon: Float,
    private val within: Boolean = true
) {
    init {
        require(epsilon >= 0f) { "Epsilon must be a non-negative number, but was $epsilon" }
    }

    // The checks below are negated so that NaN, which makes every comparison false, fails both `isWithin` and
    // `isNotWithin`.
    fun of(target: Float) {
        if (within) {
            if (!((target - parent.actual).absoluteValue <= epsilon)) {
                parent.report(
                    Report(
                        Summaries.EXPECTED_COMPARISON,
                        DetailsFor.expectedActual("within $epsilon of", target, parent.actual)
                    )
                )
            }
        }
        else {
            if (!((target - parent.actual).absoluteValue > epsilon)) {
                parent.report(
                    Report(
                        Summaries.EXPECTED_COMPARISON,
                        DetailsFor.expectedActual("not within $epsilon of", target, parent.actual)
                    )
                )
            }
        }
    }
}

class DoubleEpsilonAsserter(
    private val parent: DoubleSubject,
    private val epsilon: Double,
    private val within: Boolean = true
) {
    init {
        require(epsilon >= 0.0) { "Epsilon must be a non-negative number, but was $epsilon" }
    }

    // The checks below are negated so that NaN, which makes every comparison false, fails both `isWithin` and
    // `isNotWithin`.
    fun of(target: Double) {
        if (within) {
            if (!((target - parent.actual).absoluteValue <= epsilon)) {
                parent.report(
                    Report(
                        Summaries.EXPECTED_COMPARISON,
                        DetailsFor.expectedActual("within $epsilon of", target, parent.actual))
                )
            }
        }
        else {
            if (!((target - parent.actual).absoluteValue > epsilon)) {
                parent.report(
                    Report(
                        Summaries.EXPECTED_COMPARISON,
                        DetailsFor.expectedActual("not within $epsilon of", target, parent.actual))
                )
            }
        }
    }
}