plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(project(":"))
    testImplementation(kotlin("test-junit"))
}
