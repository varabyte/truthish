pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "truthish"

include(":truthish")
include(":integration-tests:jvm:junit4")
include(":integration-tests:jvm:junit5")

// A separate build rather than a subproject, so it can use a newer Kotlin than the rest of the project, which Konsist
// requires
includeBuild("architecture-tests")
