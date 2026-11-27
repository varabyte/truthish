package com.varabyte.truthish

import kotlin.test.Test

// In a project with neither JUnit 4 nor opentest4j on the classpath (e.g. one using TestNG), Truthish can't throw a
// failure type that test runners show a diff for, so it falls back to a plain AssertionError.
class PlainAssertionErrorTest {
    @Test
    fun neitherJUnit4NorOpenTest4jIsOnTheClasspath() {
        assertThrows<ClassNotFoundException> { Class.forName("org.junit.ComparisonFailure") }
        assertThrows<ClassNotFoundException> { Class.forName("org.opentest4j.AssertionFailedError") }
    }

    @Test
    fun isEqualToThrowsAPlainAssertionError() {
        val e = assertThrows<AssertionError> {
            assertThat("str1").isEqualTo("str2")
        }

        assertThat(e.message!!).isEqualTo(
            """
            Two objects were not equal

            Expected: "str2"
            But was : "str1"

            """.trimIndent()
        )
    }
}
