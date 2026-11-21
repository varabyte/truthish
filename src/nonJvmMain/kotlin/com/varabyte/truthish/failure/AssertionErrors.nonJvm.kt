package com.varabyte.truthish.failure

// Diff support is only hooked up on the JVM, where IntelliJ and Gradle recognize certain exception types. Other
// platforms get the plain report error.
internal actual fun Report.createAssertionError(comparison: ComparedValues?): AssertionError = ReportError(this)
