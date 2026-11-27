package com.varabyte.truthish

import com.varabyte.truthish.failure.DetailsFor
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

    @Test
    fun anonymousClassesShowQualifiedNames() {
        val first = object : Exception() {}
        val second = object : Exception() {}
        assertThrows<AssertionError> {
            assertThat(first).isInstanceOf(second::class)
        }.assertSubstrings(
            Summaries.EXPECTED_INSTANCE,
            DetailsFor.EXPECTED,
            "<anonymous> (class com.varabyte.truthish.ClassNameAsserts\$anonymousClassesShowQualifiedNames\$second\$1)",
            DetailsFor.BUT_WAS,
            "<anonymous> (class com.varabyte.truthish.ClassNameAsserts\$anonymousClassesShowQualifiedNames\$first\$1)",
        )
    }
}
