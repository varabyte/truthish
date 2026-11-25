# Architecture tests

[Konsist](https://docs.konsist.lemonappdev.com/) tests that check rules about how Truthish's code is organized.

This is a separate Gradle build (pulled in with `includeBuild` in the root `settings.gradle.kts`) rather than a
subproject, because Konsist needs a newer Kotlin version than Truthish builds with. Konsist reads Truthish's source
files directly, so this build doesn't depend on Truthish itself.

The root project's `check` task runs these tests, or run them on their own with:

```
./gradlew :architecture-tests:test
```
