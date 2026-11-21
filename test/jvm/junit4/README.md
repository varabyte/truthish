# JUnit 4 tests

Tests Truthish the way a JUnit 4 project uses it.

In a JUnit 4 project, Truthish throws JUnit's `ComparisonFailure` for equality failures, so IDEs can show a diff.

To test behavior that depends on the classpath, we put these tests in their own module.
