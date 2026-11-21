# Consumer tests

Most of Truthish's tests live in `src/commonTest` and run on every platform.

This folder holds separate modules that depend on Truthish the way a user's project would. They test behavior that
depends on the consumer's setup, such as which test framework is on the classpath.
