package com.varabyte.truthish

import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import kotlin.test.Test

class ClassNameAsserts {
    object SameHolderA { class Same }
    object SameHolderB { class Same }

    // JS doesn't support qualified class names, so the report can only say that the classes differ
    @Test
    fun sameSimpleNamesShowNote() {
        assertThrows<AssertionError> {
            assertThat(SameHolderA.Same()).isInstanceOf<SameHolderB.Same>()
        }.assertSubstrings(Summaries.EXPECTED_INSTANCE, "Same", "Same", "a different value that prints the same")
    }

    @Test
    fun anonymousClassesShowNote() {
        val first = object : Exception() {}
        val second = object : Exception() {}
        assertThrows<AssertionError> {
            assertThat(first).isInstanceOf(second::class)
        }.assertSubstrings(
            Summaries.EXPECTED_INSTANCE,
            "<anonymous>",
            "<anonymous> (a different value that prints the same)",
        )
    }
}
