package com.varabyte.truthish

import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import kotlin.test.Test

class ClassNameAsserts {
    object SameHolderA { class Same }
    object SameHolderB { class Same }

    // JS and Wasm don't support qualified class names, so the report can only say that the classes differ
    @Test
    fun sameSimpleNamesShowNote() {
        assertThrows<AssertionError> {
            assertThat(SameHolderA.Same()).isInstanceOf<SameHolderB.Same>()
        }.assertSubstrings(Summaries.EXPECTED_INSTANCE, "Same", "Same", "a different value that prints the same")
    }
}
