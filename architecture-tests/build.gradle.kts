// Konsist reads Truthish's sources from disk, so this project doesn't depend on Truthish at all
plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
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
