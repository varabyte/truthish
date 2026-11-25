package com.varabyte.truthish

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.MultipleReportsError
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import com.varabyte.truthish.failure.named
import kotlin.test.Test

class ThrowableAsserts {
    class ParseException(message: String, cause: Throwable) : Exception(message, cause)
    class DiskException(message: String) : Exception(message)
    class NoMessageException : Exception()
    class NoCauseException(message: String) : Exception(message)

    @Test
    fun throwableChecks() {
        val diskError = DiskException("disk full")
        val parseError = ParseException("unexpected end of input", cause = diskError)

        run {
            assertThat(parseError).isInstanceOf<ParseException>()
            assertThat(parseError).hasMessageThat().isEqualTo("unexpected end of input")
            assertThat(parseError).hasMessageThat().contains("end of")
            assertThat(parseError).hasCauseThat().isSameAs(diskError)
            assertThat(parseError).hasCauseThat().isInstanceOf<DiskException>()
            assertThat(parseError).hasCauseThat().hasMessageThat().startsWith("disk")
        }

        run {
            // Test false statements
            assertThrows<AssertionError> {
                assertThat(parseError).hasMessageThat().contains("empty")
            }.assertSubstrings(
                Summaries.EXPECTED_CONTAINS,
                DetailsFor.VALUE_OF, "ParseException.message",
                DetailsFor.EXPECTED, "\"empty\"",
                DetailsFor.BUT_WAS, "\"unexpected end of input\"",
            )

            assertThrows<AssertionError> {
                assertThat(NoMessageException()).hasMessageThat().contains("empty")
            }.assertSubstrings(Summaries.EXPECTED_NOT_NULL, DetailsFor.VALUE_OF, "NoMessageException.message")

            assertThrows<AssertionError> {
                assertThat(parseError).hasCauseThat().isInstanceOf<ParseException>()
            }.assertSubstrings(
                Summaries.EXPECTED_INSTANCE,
                DetailsFor.VALUE_OF, "ParseException.cause",
                DetailsFor.EXPECTED, "ParseException",
                DetailsFor.BUT_WAS, "DiskException",
            )

            assertThrows<AssertionError> {
                assertThat(NoCauseException("boom")).hasCauseThat().isInstanceOf<ParseException>()
            }.assertSubstrings(Summaries.EXPECTED_NOT_NULL, DetailsFor.VALUE_OF, "NoCauseException.cause")

            assertThrows<AssertionError> {
                assertThat(parseError).hasCauseThat().hasMessageThat().isEqualTo("disk empty")
            }.assertSubstrings(Summaries.EXPECTED_EQUAL, DetailsFor.VALUE_OF, "ParseException.cause.message")

            // Anonymous classes have no name of their own
            assertThrows<AssertionError> {
                assertThat(object : Exception("boom") {}).hasMessageThat().contains("empty")
            }.assertSubstrings(DetailsFor.VALUE_OF, "Throwable.message")

            // A name given by the user is kept
            assertThrows<AssertionError> {
                assertThat(parseError).named("Config error").hasMessageThat().contains("empty")
            }.assertSubstrings(DetailsFor.NAME, "Config error", DetailsFor.VALUE_OF, "ParseException.message")
            assertThrows<AssertionError> {
                assertThat(parseError).hasCauseThat().named("IO error").hasMessageThat().contains("empty")
            }.assertSubstrings(DetailsFor.NAME, "IO error", DetailsFor.VALUE_OF, "ParseException.cause.message")

            assertThrows<AssertionError> {
                assertWithMessage(TEST_MESSAGE).that(parseError).hasCauseThat().hasMessageThat().contains("empty")
            }.assertSubstrings(DetailsFor.MESSAGE, TEST_MESSAGE, DetailsFor.VALUE_OF, "ParseException.cause.message")
        }
    }

    @Test
    fun missingMessageIsReportedOnceInAssertAll() {
        assertThrows<MultipleReportsError> {
            assertAll {
                that(NoMessageException()).hasMessageThat().contains("empty")
                that(NoCauseException("boom")).hasCauseThat().hasMessageThat().contains("empty")
            }
        }.let { e ->
            assertThat(e.reports.size).isEqualTo(2)
            e.assertSubstrings(
                Summaries.EXPECTED_NOT_NULL, DetailsFor.VALUE_OF, "NoMessageException.message",
                Summaries.EXPECTED_NOT_NULL, DetailsFor.VALUE_OF, "NoCauseException.cause",
            )
        }
    }
}
