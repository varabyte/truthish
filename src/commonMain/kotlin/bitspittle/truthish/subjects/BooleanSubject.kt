package bitspittle.truthish.subjects

import bitspittle.truthish.failure.Report
import bitspittle.truthish.failure.Summaries

class BooleanSubject(private val actual: Boolean) : NotNullSubject<Boolean>(actual) {
    fun isTrue() {
        if (!actual) {
            report(Report(Summaries.EXPECTED_TRUE))
        }
    }

    fun isFalse() {
        if (actual) {
            report(Report(Summaries.EXPECTED_FALSE))
        }
    }
}
