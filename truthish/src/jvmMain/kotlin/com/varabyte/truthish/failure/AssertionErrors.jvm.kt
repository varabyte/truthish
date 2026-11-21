package com.varabyte.truthish.failure

import org.junit.ComparisonFailure
import org.opentest4j.AssertionFailedError

// Truthish only has a compile-time dependency on these libraries, so check that they exist before loading any class
// that references them.
private fun isClassAvailable(name: String) = try {
    Class.forName(name, /* initialize = */ false, Report::class.java.classLoader)
    true
} catch (_: ClassNotFoundException) {
    false
}

private val isOpenTest4jAvailable by lazy { isClassAvailable("org.opentest4j.AssertionFailedError") }
private val isJUnit4Available by lazy { isClassAvailable("org.junit.ComparisonFailure") }

internal actual fun Report.createAssertionError(comparison: ComparedValues?): AssertionError {
    if (comparison == null) return ReportError(this)
    return when {
        isOpenTest4jAvailable -> ReportAssertionFailedError(this, comparison)
        isJUnit4Available -> ReportComparisonFailure(this, comparison)
        else -> ReportError(this)
    }
}

private class ReportAssertionFailedError(report: Report, comparison: ComparedValues) :
    AssertionFailedError(report.toString(), comparison.expected, comparison.actual)

private class ReportComparisonFailure(private val report: Report, comparison: ComparedValues) :
    ComparisonFailure(report.toString(), comparison.expected, comparison.actual) {
    // ComparisonFailure otherwise rewrites the message into JUnit's compact "expected:<...> but was:<...>" form.
    override val message: String get() = report.toString()
}
