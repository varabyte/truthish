package com.varabyte.truthish

import com.varabyte.truthish.failure.assertSubstrings
import kotlin.test.Test

class AmbiguityTest {
    class ClassA(private val value: String) {
        override fun toString() = value
    }

    class ClassB(private val value: String) {
        override fun toString() = value
    }

    @Test
    fun extraInformationShownIfToStringIsAmbiguous() {
        assertThrows<AssertionError> {
            assertThat(ClassA("x")).isEqualTo(ClassB("x"))
        }.assertSubstrings("x (Type: `ClassB`)", "x (Type: `ClassA`)")

        assertThrows<AssertionError> {
            assertThat(ClassA("x")).isEqualTo("x")
        }.assertSubstrings("\"x\" (Type: `String`)", "x (Type: `ClassA`)")

        assertThrows<AssertionError> {
            assertThat("x").isEqualTo(ClassB("x"))
        }.assertSubstrings("x (Type: `ClassB`)", "\"x\" (Type: `String`)")
    }
}
