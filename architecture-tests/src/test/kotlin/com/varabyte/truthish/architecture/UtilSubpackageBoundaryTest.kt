package com.varabyte.truthish.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class UtilSubpackageBoundaryTest {
    // `util` holds code that Truthish uses but that could, in theory, be pulled out and used in another project, so
    // each of its subpackages (e.g. `util.diff`) may only depend on itself and the Kotlin standard library.
    //
    // Another approach was to create a separate utility module, but that seemed needlessly heavy (requiring a new
    // artifact), so we decided to use this test approach for now instead.
    @Test
    fun utilSubpackageOnlyDependsOnItself() {
        Konsist.scopeFromProduction()
            .files
            .filter { it.packagee?.name?.startsWith("com.varabyte.truthish.util") == true }
            .assertTrue(
                additionalMessage = "Code in util is not expected to escape its subpackage -- it is supposed to be " +
                        "self-contained. If this rule breaks for an intentional reason, update this test.",
            ) { file ->
                // Each subpackage under util is its own sandbox, e.g. anything under "com.varabyte.truthish.util.diff"
                // should only import Truthish code from under "util.diff"
                val ownSubpackage = file.packagee!!.name.split('.').take(5).joinToString(".")
                file.imports.none { import ->
                    import.name.startsWith("com.varabyte.truthish.") && !import.name.startsWith("$ownSubpackage.")
                }
            }
    }

    // `util` itself is only an organizational root, so it can't become a dumping ground for code the subpackages share.
    @Test
    fun utilPackageHasNoCodeOfItsOwn() {
        Konsist.scopeFromProduction()
            .files
            .assertFalse(
                additionalMessage = "Code in util should live in a subpackage, e.g. util.diff, not in util itself.",
            ) { file -> file.packagee?.name == "com.varabyte.truthish.util" }
    }
}
