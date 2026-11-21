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
