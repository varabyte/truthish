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
    fun noDiffWhenValuesPrintTheSame() {
        data class Point(val x: Int, val y: Int)

        val e = assertThrows<AssertionError> {
            assertThat(Point(1, 2)).isSameAs(Point(1, 2))
        }
        assertThat(e).isNotInstanceOf<ComparisonFailure>()
    }
}
