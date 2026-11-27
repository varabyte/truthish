# Architecture tests

[Konsist](https://docs.konsist.lemonappdev.com/) tests that check rules about how Truthish's code is organized.

Konsist reads Truthish's source files directly, so this project doesn't depend on Truthish itself.

The root project's `check` task runs these tests, or run them on their own with:

```
./gradlew :architecture-tests:test
```
