package com.github.bitspittle.truthish.subjects

import com.github.bitspittle.truthish.failure.Report
import com.github.bitspittle.truthish.failure.Summaries

class BooleanSubject(private val actual: Boolean) : ComparableSubject<Boolean>(actual) {
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
