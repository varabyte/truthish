plugins {
    kotlin("jvm")
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
