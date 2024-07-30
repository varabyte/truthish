package com.varabyte.truthish

import com.varabyte.truthish.failure.withStrategy
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
        val testStrategy = TestStrategy()

        assertThat(ClassA("x")).withStrategy(testStrategy).isEqualTo(ClassB("x"))
        testStrategy.verifyFailureAndClear("x (Type: `ClassB`)", "x (Type: `ClassA`)")

        assertThat(ClassA("x")).withStrategy(testStrategy).isEqualTo("x")
        testStrategy.verifyFailureAndClear("\"x\" (Type: `String`)", "x (Type: `ClassA`)")

        assertThat("x").withStrategy(testStrategy).isEqualTo(ClassB("x"))
        testStrategy.verifyFailureAndClear("x (Type: `ClassB`)", "\"x\" (Type: `String`)")
    }
}
