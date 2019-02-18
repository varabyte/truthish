## Truthish

A testing API inspired by [Google truth](https://github.com/google/truth) but
rewritten in Kotlin from the ground up so it can be used in Kotlin
multiplatform projects.

# Using Truthish in Your Project

*Truthish* is 100% Kotlin, so unlike some multiplatform libraries, you only need
to reference it in your `commonTest` dependencies:

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
            implementation "io.github.bitspittle:truthish:$version"
        }
    }
    ...
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

*Truthish* is a multiplatform library, meaning there really are multiple
versions of it, and you may want to test against all of them. However, it's
easiest to test against the JVM target, so let's start with that.

Simply run

`./gradlew jvmTest`

If using IntelliJ, you can double-click this task through the Gradle toolbar.

`truthish > Tasks > verification > jvmTest`

Once you've done that, IntelliJ will save it as a reusable run configuration,
at which point you can rerun the task using a debugger.

*TODO: Instructions for JS*
