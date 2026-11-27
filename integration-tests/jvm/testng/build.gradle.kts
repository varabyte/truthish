plugins {
    kotlin("jvm")
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
