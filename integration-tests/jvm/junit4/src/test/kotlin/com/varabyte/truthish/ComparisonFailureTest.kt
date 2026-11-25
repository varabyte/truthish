package com.varabyte.truthish

import org.junit.ComparisonFailure
import kotlin.test.Test

// In a JUnit 4 project, test runners like IntelliJ and Gradle offer a "Click to see difference" view when a failure is
// a ComparisonFailure carrying expected and actual values.
//
// The common tests can't verify this: ComparisonFailure is a JVM-only JUnit class, so common code can't refer to it.
// So we handle those tests here.
class ComparisonFailureTest {
    @Test
    fun isEqualToOffersDiff() {
        val e = assertThrows<ComparisonFailure> {
            assertThat("str1").isEqualTo("str2")
        }

        assertThat(e.expected).isEqualTo("\"str2\"")
        assertThat(e.actual).isEqualTo("\"str1\"")
        assertThat(e.message!!).isEqualTo(
            """
            Two objects were not equal

            Expected: "str2"
            But was : "str1"

            """.trimIndent()
        )
    }

    @Test
    fun diffValuesIncludeTypeHints() {
        val e = assertThrows<ComparisonFailure> {
            assertThat(1L).isEqualTo(1)
        }

        assertThat(e.expected).isEqualTo("1 (Type: `Int`)")
        assertThat(e.actual).isEqualTo("1 (Type: `Long`)")
    }

    @Test
    fun diffValuesAreNotTruncated() {
        val e = assertThrows<ComparisonFailure> {
            assertThat((1..12).toList()).isEqualTo((1..13).toList())
        }

        assertThat(e.expected).isEqualTo("[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ]")
        assertThat(e.actual).isEqualTo("[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 ]")
        assertThat(e.message!!).isEqualTo(
            """
            Two objects were not equal

            Expected: [ … (2 more), 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ]
            But was : [ … (2 more), 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 ]
            Missing : [ 13 ]

            """.trimIndent()
        )
    }

    @Test
    fun multilineStringsAreReportedAsALineDiff() {
        val e = assertThrows<ComparisonFailure> {
            assertThat("a\nb\nc").isEqualTo("a\nB\nc")
        }

        assertThat(e.expected).isEqualTo("a\nB\nc")
        assertThat(e.actual).isEqualTo("a\nb\nc")
        assertThat(e.message!!).isEqualTo(
            """
            Two objects were not equal

            Diff:   a
                  - B
                  + b
                    c

            """.trimIndent()
        )
    }

    // The IDE's diff viewer handles line endings and control characters itself (e.g. it notes differing line
    // separators), so they should reach it unchanged, even though the report's own diff ignores line endings
    @Test
    fun multilineStringsAreReportedWithControlCharactersPassedThroughUnchanged() {
        val e = assertThrows<ComparisonFailure> {
            assertThat("val s = \"a\"\r\n\tprint(s)\u001B[0m\n").isEqualTo("val s = \"b\"\r\n\tprint(s)\n")
        }

        assertThat(e.expected).isEqualTo("val s = \"b\"\r\n\tprint(s)\n")
        assertThat(e.actual).isEqualTo("val s = \"a\"\r\n\tprint(s)\u001B[0m\n")
    }

    @Test
    fun noDiffWhenValuesPrintTheSame() {
        data class Point(val x: Int, val y: Int)

        val e = assertThrows<AssertionError> {
            assertThat(Point(1, 2)).isSameAs(Point(1, 2))
        }
        assertThat(e).isNotInstanceOf<ComparisonFailure>()
    }
}
