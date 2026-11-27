plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(project(":truthish"))
    testImplementation(kotlin("test-testng"))
}

tasks.test {
    useTestNG()
}
