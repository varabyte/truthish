package com.varabyte.truthish

import org.opentest4j.AssertionFailedError
import kotlin.test.Test

// In a JUnit 5 project, test runners like IntelliJ and Gradle offer a "Click to see difference" view when a failure is
// an opentest4j AssertionFailedError carrying expected and actual values.
//
// The common tests can't verify this: AssertionFailedError is a JVM-only opentest4j class, so common code can't refer
// to it. So we handle those tests here.
class AssertionFailedErrorTest {
    @Test
    fun isEqualToOffersDiff() {
        val e = assertThrows<AssertionFailedError> {
            assertThat("str1").isEqualTo("str2")
        }

        assertThat(e.expected.value).isEqualTo("\"str2\"")
        assertThat(e.actual.value).isEqualTo("\"str1\"")
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
        val e = assertThrows<AssertionFailedError> {
            assertThat(1L).isEqualTo(1)
        }

        assertThat(e.expected.value).isEqualTo("1 (Type: `Int`)")
        assertThat(e.actual.value).isEqualTo("1 (Type: `Long`)")
    }

    @Test
    fun diffValuesAreNotTruncated() {
        val e = assertThrows<AssertionFailedError> {
            assertThat((1..12).toList()).isEqualTo((1..13).toList())
        }

        assertThat(e.expected.value).isEqualTo("[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ]")
        assertThat(e.actual.value).isEqualTo("[ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 ]")
        assertThat(e.message!!).isEqualTo(
            """
            Two objects were not equal

            Expected: [ … (2 more), 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ]
            But was : [ … (2 more), 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 ]

            """.trimIndent()
        )
    }

    @Test
    fun multilineStringsAreReportedAsALineDiff() {
        val e = assertThrows<AssertionFailedError> {
            assertThat("a\nb\nc").isEqualTo("a\nB\nc")
        }

        assertThat(e.expected.value).isEqualTo("a\nB\nc")
        assertThat(e.actual.value).isEqualTo("a\nb\nc")
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
        val e = assertThrows<AssertionFailedError> {
            assertThat("val s = \"a\"\r\n\tprint(s)\u001B[0m\n").isEqualTo("val s = \"b\"\r\n\tprint(s)\n")
        }

        assertThat(e.expected.value).isEqualTo("val s = \"b\"\r\n\tprint(s)\n")
        assertThat(e.actual.value).isEqualTo("val s = \"a\"\r\n\tprint(s)\u001B[0m\n")
    }

    @Test
    fun noDiffWhenValuesPrintTheSame() {
        data class Point(val x: Int, val y: Int)

        val e = assertThrows<AssertionError> {
            assertThat(Point(1, 2)).isSameAs(Point(1, 2))
        }
        assertThat(e).isNotInstanceOf<AssertionFailedError>()
    }
}
