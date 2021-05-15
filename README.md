## Truthish

[![version](https://img.shields.io/badge/version-0.6.0-yellow.svg)](https://semver.org)

A testing API inspired by [Google Truth](https://github.com/google/truth) but
rewritten in Kotlin from the ground up, so it can be used in Kotlin
multiplatform projects.

For example, you can write `assertThat` checks in tests like this:

```kotlin
import com.github.bitspittle.truthish

fun isEven(num: Int) = (num % 2) == 0
fun square(num: Int) = (num * num)

@Test
fun testEvenOdd() {
    assertThat(isEven(1234)).isTrue()
    assertThat(isEven(1235)).isFalse()
}

@Test
fun testSum() {
    val nums = listOf(1, 2, 3, 4, 5)
    assertThat(nums.sum()).isEqualTo(15)
}

@Test
fun testMap() {
    assertThat(listOf(1, 2, 3, 4, 5).map { square(it) })
        .containsExactly(1, 4, 9, 16, 25)
        .inOrder()
}

@Test
fun customMessage() {
    assertWithMessage("Unexpected list size")
        .that(listOf(1, 2, 3, 4, 5)).hasSize(5)
}

@Test
fun testDivideByZeroException() {
    val ex = assertThrows<ArithmeticException> {
        10 / 0
    }
    assertThat(ex.message).isEqualTo("/ by zero")
}
```

You can read the [Google Truth documentation](https://truth.dev/) for why they
believe their fluent approach to assertions is both more readable and produces
cleaner error messages, but let's break one of the tests above to see a
specific example error message:

```kotlin
@Test
fun testMapButIntentionallyBroken() {
    assertThat(listOf(1, 2, 3, 4, 5).map { square(it) })
        .containsExactly(1, 4, 9, 15, 26) // <-- Ooops, messed up 16 and 25 here
        .inOrder()
}
```

Output:

```text
A collection did not contain element(s)

Expected exactly all elements from: [ 1, 4, 9, 15, 26 ]
But was                           : [ 1, 4, 9, 16, 25 ]
Missing                           : [ 15, 26 ]
Extraneous                        : [ 16, 25 ]
```

# Using Truthish in Your Project

> **Note**: Truthish is not currently hosted anywhere, so to use it you'll
first need to clone the repository and run `/.gradlew publishToMavenLocal`.
Then, make sure you specify `mavenLocal` as a repository in your own project.
If there's demand for this library, let me know, and I'll investigate getting
it hosted appropriately.

To use *Truthish* in your multiplatform application, declare the following
dependencies:

```groovy
// build.gradle

repositories {
    mavenLocal()
    // ...
}

kotlin {
  jvm()
  js()
  // ...
  sourceSets {
    // ...
    commonTest {
        dependencies {
            implementation kotlin("test-common")
            implementation kotlin("test-annotations-common")
            // ...
            implementation "com.github.bitspittle:truthish:$truthish-version"
        }
    }
    
    jmvTest {
        dependencies {
            implementation kotlin("test")
            // ...
            implementation "com.github.bitspittle:truthish-jvm:$truthish-version"
        }
    }

    jsTest {
        dependencies {
            implementation kotlin("test-js")
            // ...
            implementation "com.github.bitspittle:truthish-js:$truthish-version"
        }
    }
  }
}
```