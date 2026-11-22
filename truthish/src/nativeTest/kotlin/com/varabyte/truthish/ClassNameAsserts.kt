package com.varabyte.truthish

import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import kotlin.test.Test

class ClassNameAsserts {
    object SameHolderA { class Same }
    object SameHolderB { class Same }

    @Test
    fun sameSimpleNamesShowQualifiedNames() {
        assertThrows<AssertionError> {
            assertThat(SameHolderA.Same()).isInstanceOf<SameHolderB.Same>()
        }.assertSubstrings(Summaries.EXPECTED_INSTANCE, "Same", "SameHolderB.Same", "Same", "SameHolderA.Same")
    }
}
