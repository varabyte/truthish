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

    class TestEntry(override val key: String, override val value: Int) : Map.Entry<String, Int>

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

        assertThrows<AssertionError> {
            assertThat(ClassA("x")).isEqualTo(ClassA("x"))
        }.assertSubstrings("x", "x (a different value that prints the same)")
    }

    @Test
    fun anonymousTypesAreReportedAsAnonymous() {
        assertThrows<AssertionError> {
            assertThat(object { override fun toString() = "x" }).isEqualTo("x")
        }.assertSubstrings("\"x\" (Type: `String`)", "x (Type: `<anonymous>`)")
    }

    @Test
    fun mapEntriesAndPairsPrintTheSameButReportTheirTypes() {
        assertThrows<AssertionError> {
            assertThat(TestEntry("a", 1)).isEqualTo("a" to 1)
        }.assertSubstrings("\"a\" to 1 (Type: `Pair`)", "\"a\" to 1 (Type: `TestEntry`)")
    }
}
