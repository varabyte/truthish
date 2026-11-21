pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "truthish"

include(":test:jvm:junit4")
include(":test:jvm:junit5")
