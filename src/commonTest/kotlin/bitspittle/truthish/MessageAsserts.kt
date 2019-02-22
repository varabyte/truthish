package bitspittle.truthish

import bitspittle.truthish.failure.withStrategy
import kotlin.test.Test

const val TEST_MESSAGE = "Your message here"

class MessageAsserts {
    class Stub

    class IntValue(val value: Int) : Comparable<IntValue> {
        override fun compareTo(other: IntValue): Int {
            return value.compareTo(other.value)
        }
    }

    @Test
    fun messageChecks() {
        val testStrategy = TestStrategy()

        val stub: Stub? = Stub()
        assertWithMessage(TEST_MESSAGE).that(stub).withStrategy(testStrategy).isNull()
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(stub!!).withStrategy(testStrategy).isNotInstanceOf<Stub>()
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(IntValue(3)).withStrategy(testStrategy).isGreaterThan(IntValue(4))
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(false).withStrategy(testStrategy).isTrue()
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(10).withStrategy(testStrategy).isGreaterThanEqual(100)
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(10.toByte()).withStrategy(testStrategy).isGreaterThanEqual(100.toByte())
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(10.toShort()).withStrategy(testStrategy).isGreaterThanEqual(100.toShort())
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(10.toLong()).withStrategy(testStrategy).isGreaterThanEqual(100.toLong())
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(10f).withStrategy(testStrategy).isGreaterThanEqual(100f)
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(10.0).withStrategy(testStrategy).isGreaterThanEqual(100.0)
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that("XYZ").withStrategy(testStrategy).isEmpty()
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(listOf(1, 2, 3)).withStrategy(testStrategy).containsAnyIn(listOf(4, 5, 6))
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)
    }
}