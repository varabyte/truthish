plugins {
    kotlin("multiplatform") version "2.1.0" apply false
    kotlin("jvm") version "2.1.0" apply false
    id("org.jetbrains.dokka") version "2.1.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.4" apply false
    id("com.vanniktech.maven.publish") version "0.35.0" apply false
}

// The Kotlin plugin registers some tasks on the root project (e.g. the native commonizer) that resolve dependencies.
repositories {
    mavenCentral()
}
