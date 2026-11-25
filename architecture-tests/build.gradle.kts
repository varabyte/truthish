// A separate build, so it can use a newer Kotlin than Truthish itself, which Konsist needs. Konsist reads Truthish's
// sources from disk, so this build doesn't depend on Truthish at all.
plugins {
    alias(libs.plugins.kotlin.jvm.architecture)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    // Gradle can't see that the tests read Truthish's sources, so declare them, or a change there won't rerun the tests
    inputs.dir("../truthish/src").withPathSensitivity(PathSensitivity.RELATIVE)
}
