package com.varabyte.truthish

import com.varabyte.truthish.failure.withStrategy
import kotlin.test.Test

class MapAsserts {
    @Test
    fun mapChecks() {
        val asciiMap = ('a'..'z').associateWith { it.code }

        run {
            assertThat(asciiMap).isEqualTo(asciiMap.toMap())
            assertThat(asciiMap).isNotEqualTo(emptyMap())

            assertThat(asciiMap).containsKey('g')
            assertThat(asciiMap.keys).contains('g')
            assertThat(asciiMap).doesNotContainKey('G')
            assertThat(asciiMap.keys).doesNotContain('G')
            assertThat(asciiMap).containsValue(107)
            assertThat(asciiMap.values).contains(107)
            assertThat(asciiMap).doesNotContainValue(9999)
            assertThat(asciiMap.values).doesNotContain(9999)
            assertThat(asciiMap).contains('e' to 101)
            assertThat(asciiMap).doesNotContain('e' to 102)

            assertThat(asciiMap).containsAnyIn('a' to 97, 'a' to 98)
            assertThat(asciiMap).containsAllIn('a' to 97, 'z' to 122).inOrder()
            assertThat(asciiMap).containsNoneIn('a' to 122, 'z' to 97)
            assertThat(asciiMap).containsAnyIn(mapOf('a' to 97, '?' to 9999))
            assertThat(asciiMap).containsAllIn(mapOf('a' to 97, 'z' to 122)).inOrder()
            assertThat(asciiMap).containsNoneIn(mapOf('a' to 122, 'z' to 97))

            assertThat(asciiMap).containsExactly(asciiMap.toMap())
        }

        run {
            // Test false statements
            val testStrategy = TestStrategy()

            assertThat(asciiMap).withStrategy(testStrategy).isEqualTo(emptyMap())
            testStrategy.verifyFailureAndClear()

            assertThat(asciiMap).withStrategy(testStrategy).isNotEqualTo(asciiMap)
            testStrategy.verifyFailureAndClear()


            assertThat(asciiMap).withStrategy(testStrategy).doesNotContainKey('g')
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap.keys).withStrategy(testStrategy).doesNotContain('g')
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap).withStrategy(testStrategy).containsKey('G')
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap.keys).withStrategy(testStrategy).contains('G')
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap).withStrategy(testStrategy).doesNotContainValue(107)
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap.values).withStrategy(testStrategy).doesNotContain(107)
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap).withStrategy(testStrategy).containsValue(9999)
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap.values).withStrategy(testStrategy).contains(9999)
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap).withStrategy(testStrategy).doesNotContain('e' to 101)
            testStrategy.verifyFailureAndClear()
            assertThat(asciiMap).withStrategy(testStrategy).contains('e' to 102)
            testStrategy.verifyFailureAndClear()

            assertThat(asciiMap).withStrategy(testStrategy).containsAllIn('a' to 122, 'z' to 122)
            testStrategy.verifyFailureAndClear()

            assertThat(asciiMap).withStrategy(testStrategy).containsAllIn('z' to 122, 'a' to 97).inOrder()
            testStrategy.verifyFailureAndClear()
        }
    }
}