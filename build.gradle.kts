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
