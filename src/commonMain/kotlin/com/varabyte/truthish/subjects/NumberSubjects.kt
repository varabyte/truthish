package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Summaries
import kotlin.math.absoluteValue

class ByteSubject(actual: Byte) : ComparableSubject<Byte>(actual)
class ShortSubject(actual: Short) : ComparableSubject<Short>(actual)
class IntSubject(actual: Int) : ComparableSubject<Int>(actual)
class LongSubject(actual: Long) : ComparableSubject<Long>(actual)

class FloatSubject(internal val actual: Float) : ComparableSubject<Float>(actual) {
    fun isWithin(epsilon: Float) = FloatEpsilonAsserter(this, epsilon)
    fun isNotWithin(epsilon: Float) = FloatEpsilonAsserter(this, epsilon, within = false)
}

class DoubleSubject(internal val actual: Double) : ComparableSubject<Double>(actual) {
    fun isWithin(epsilon: Double) = DoubleEpsilonAsserter(this, epsilon)
    fun isNotWithin(epsilon: Double) = DoubleEpsilonAsserter(this, epsilon, within = false)
}

class FloatEpsilonAsserter(
    private val parent: FloatSubject,
    private val epsilon: Float,
    private val within: Boolean = true
) {
    fun of(target: Float) {
        if (within) {
            if ((target - parent.actual).absoluteValue > epsilon) {
                parent.report(
                    Report(
                        Summaries.EXPECTED_COMPARISON,
                        DetailsFor.expectedActual("within $epsilon of", parent.actual, target)
                    )
                )
            }
        }
        else {
            if ((target - parent.actual).absoluteValue <= epsilon) {
                parent.report(
                    Report(
                        Summaries.EXPECTED_COMPARISON,
                        DetailsFor.expectedActual("not within $epsilon of", parent.actual, target)
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
    fun of(target: Double) {
        if (within) {
            if ((target - parent.actual).absoluteValue > epsilon) {
                parent.report(
                    Report(
                        Summaries.EXPECTED_COMPARISON,
                        DetailsFor.expectedActual("within $epsilon of", parent.actual, target))
                )
            }
        }
        else {
            if ((target - parent.actual).absoluteValue <= epsilon) {
                parent.report(
                    Report(
                        Summaries.EXPECTED_COMPARISON,
                        DetailsFor.expectedActual("not within $epsilon of", parent.actual, target))
                )
            }
        }
    }
}