plugins {
    base
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.maven.publish) apply false
}

// The Kotlin plugin registers some tasks on the root project (e.g. the native commonizer) that resolve dependencies.
repositories {
    mavenCentral()
}

// The architecture tests live in their own build (see architecture-tests/build.gradle.kts), so hook them in here
tasks.check { dependsOn(gradle.includedBuild("architecture-tests").task(":test")) }
