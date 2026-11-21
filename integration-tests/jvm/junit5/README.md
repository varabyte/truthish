# JUnit 5 tests

Tests Truthish the way a JUnit 5 project uses it.

In a JUnit 5 project, Truthish throws opentest4j's `AssertionFailedError` (which JUnit 5 depends on) for equality
failures, so IDEs can show a diff.

To test behavior that depends on the classpath, we put these tests in their own module.
