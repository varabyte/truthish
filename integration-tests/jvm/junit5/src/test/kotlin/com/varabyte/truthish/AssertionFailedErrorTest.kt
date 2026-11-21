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
    fun noDiffWhenValuesPrintTheSame() {
        data class Point(val x: Int, val y: Int)

        val e = assertThrows<AssertionError> {
            assertThat(Point(1, 2)).isSameAs(Point(1, 2))
        }
        assertThat(e).isNotInstanceOf<AssertionFailedError>()
    }
}
