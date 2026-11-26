package com.varabyte.truthish.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class EntryPointsKeptInSyncTest {
    // Each top-level `assertThat` delegates to the `that` extension with the same signature. If that extension is
    // missing, the call quietly resolves to a less specific one instead, e.g. `that(actual: Any)`.
    @Test
    fun assertThatAndThatOverloadsMatch() {
        val functions = Konsist.scopeFromProduction()
            .functions()
            .filter { it.isTopLevel && it.resideInPackage("com.varabyte.truthish") }
        val thatFunctions = functions.filter { it.name == "that" && it.receiverType?.name == "AssertionEntryPoint" }
        val assertThatFunctions = functions.filter { it.name == "assertThat" && it.receiverType == null }
        val thatSignatures = thatFunctions.map { it.toSignature() }.toSet()
        val assertThatSignatures = assertThatFunctions.map { it.toSignature() }.toSet()

        assertThatFunctions.assertTrue(
            additionalMessage = "Add a matching `that` extension in AssertionEntryPoint.kt.",
        ) { it.toSignature() in thatSignatures }
        thatFunctions.assertTrue(
            additionalMessage = "Add a matching top-level `assertThat` in Truth.kt.",
        ) { it.toSignature() in assertThatSignatures }
    }
}

/** The type parameters and parameter types, e.g. `[T: Comparable<T>, T]`, which two matching overloads share. */
private fun KoFunctionDeclaration.toSignature() = typeParameters.map { it.text } + parameters.map { it.type.text }
