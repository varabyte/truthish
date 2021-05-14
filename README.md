## Truthish

A testing API inspired by [Google truth](https://github.com/google/truth) but
rewritten in Kotlin from the ground up so it can be used in Kotlin
multiplatform projects.

# Using Truthish in Your Project

To use *Truthish* in your multiplatform application, declare the following
dependencies:

```
build.gradle

kotlin {
  jvm()
  js()
  ...
  sourceSets {
    ...
    commonTest {
        dependencies {
            implementation kotlin("test-common")
            implementation kotlin("test-annotations-common")
            ...
            implementation "io.github.bitspittle:truthish:$truthish-version"
        }
    }
    
    jmvTest {
        dependencies {
            implementation kotlin("test")
            ...
            implementation "io.github.bitspittle:truthish-jvm:$truthish-version"
        }
    }

    jsTest {
        dependencies {
            implementation kotlin("test-js")
            ...
            implementation "io.github.bitspittle:truthish-js:$truthish-version"
        }
    }
  }
}
```

# Editing Truthish

*Truthish* is designed to rely on Gradle and, therefore, work in any code
editing environment. However, using IntelliJ is recommended, for its top-notch
Kotlin support.

To open *Truthish* the first time, simply import the `truthish` root folder,
using Gradle as the external model. Using all default settings should work
just fine.

# Testing Truthish

To test *Truthish* on all platforms, simply run

`./gradlew check`

For JVM: `/.gradlew jvmTest`
For JS: `/.gradlew jsTest`

If using IntelliJ, you can double-click these tasks through the Gradle toolbar.

For example, `truthish > Tasks > verification > jvmTest` for JVM tests.

