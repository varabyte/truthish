package com.varabyte.truthish.subjects

import com.varabyte.truthish.failure.Report
import com.varabyte.truthish.failure.Summaries

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