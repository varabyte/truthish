package com.varabyte.truthish.failure

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.util.diff.LineDiff
import kotlin.test.Test

// Most stringifiers are tested indirectly through the assertions that use them. This one gets its own tests because it
// has logic of its own (choosing which lines to show and collapsing the rest) whose edge cases are awkward to reach
// through an assertion.
class LineDiffStringifierTest {
    @Test
    fun linesAreMarkedAsAddedOrRemoved() {
        val diff = LineDiff(
            "fun main() {\n    val greeting = \"Hello\"\n    println(greeting)\n}",
            "fun main() {\n    val greeting = \"Hello, world\"\n    println(greeting)\n}",
        )
        assertThat(LineDiffStringifier(diff).render()).isEqualTo(
            """
              fun main() {
            -     val greeting = "Hello"
            +     val greeting = "Hello, world"
                  println(greeting)
              }
            """.trimIndent()
        )
    }

    @Test
    fun linesOutsideTheActiveWindowAreCollapsed() {
        val expected = (1..20).joinToString("\n") { "line $it" }
        val actual = expected.replace("line 10", "line ten")
        assertThat(LineDiffStringifier(LineDiff(expected, actual)).render()).isEqualTo(
            """
              … (6 unchanged lines)
              line 7
              line 8
              line 9
            - line 10
            + line ten
              line 11
              line 12
              line 13
              … (7 unchanged lines)
            """.trimIndent()
        )
    }

    // There's no point is showing "… (1 unchanged lines)" when we could just show the line instead
    @Test
    fun longDiffIsCutOff() {
        val diff = LineDiff((1..40).joinToString("\n"), (1..40).joinToString("\n") { "line $it" })
        assertThat(LineDiffStringifier(diff).render()).isEqualTo(
            """
            - 1
            - 2
            - 3
            - 4
            - 5
            - 6
            - 7
            - 8
            - 9
            - 10
            - 11
            - 12
            - 13
            - 14
            - 15
            - 16
            - 17
            - 18
            - 19
            - 20
            - 21
            - 22
            - 23
            - 24
            - 25
            - 26
            - 27
            - 28
            - 29
              … (51 more changed lines)
            """.trimIndent()
        )
    }

    @Test
    fun loneLineIsShownInsteadOfCollapsed() {
        val expected = (1..9).joinToString("\n") { "line $it" }
        val actual = expected.replace("line 5", "line five")
        assertThat(LineDiffStringifier(LineDiff(expected, actual)).render()).isEqualTo(
            """
              line 1
              line 2
              line 3
              line 4
            - line 5
            + line five
              line 6
              line 7
              line 8
              line 9
            """.trimIndent()
        )
    }

    @Test
    fun distantChangesGetSeparateWindows() {
        val expected = (1..100).joinToString("\n")
        val actual = expected.replace("15", "fifteen").replace("85", "eighty-five")
        assertThat(LineDiffStringifier(LineDiff(expected, actual)).render()).isEqualTo(
            """
              … (11 unchanged lines)
              12
              13
              14
            - 15
            + fifteen
              16
              17
              18
              … (63 unchanged lines)
              82
              83
              84
            - 85
            + eighty-five
              86
              87
              88
              … (12 unchanged lines)
            """.trimIndent()
        )
    }

    // In a real report, the final line would be followed by a note like:
    // (expected ends with a newline but actual doesn't)
    @Test
    fun withoutChangedLinesTheEndIsShown() {
        val diff = LineDiff("1\n2\n3\n4\n5\n6", "1\n2\n3\n4\n5\n6\n")
        assertThat(LineDiffStringifier(diff).render()).isEqualTo(
            """
            |  … (3 unchanged lines)
            |  4
            |  5
            |  6
            """.trimMargin()
        )
    }

    @Test
    fun invisibleCharactersAreShownAsSymbols() {
        val diff = LineDiff("\tsay(\"\u001B[31mhi\u001B[0m\")\r\u0085\nend", "\tsay(\"bye\")\nend")
        assertThat(LineDiffStringifier(diff).render()).isEqualTo(
            """
            - →say("␛[31mhi␛[0m")␍<U+0085>
            + →say("bye")
              end
            """.trimIndent()
        )
    }

    @Test
    fun backslashesAndQuotesAreShownAsIs() {
        val diff = LineDiff("path = \"C:\\temp\"\nend", "path = \"C:\\tmp\"\nend")
        assertThat(LineDiffStringifier(diff).render()).isEqualTo(
            """
            - path = "C:\temp"
            + path = "C:\tmp"
              end
            """.trimIndent()
        )
    }

    @Test
    fun emptyLinesHaveNoTrailingSpaces() {
        val diff = LineDiff("a\n\nb\n\n", "a\n\nc")
        assertThat(LineDiffStringifier(diff).render()).isEqualTo(
            """
              a

            - b
            -
            + c
            """.trimIndent()
        )
    }
}
