plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(project(":truthish"))
    testImplementation(kotlin("test-junit5"))
}

tasks.test {
    useJUnitPlatform()
}
