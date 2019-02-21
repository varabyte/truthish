package bitspittle.truthish

import bitspittle.truthish.failure.withStrategy
import kotlin.test.Test

const val TEST_MESSAGE = "Your message here"

class MessageAsserts {
    class Stub

    @Test
    fun messageChecks() {
        val testStrategy = TestStrategy()

        val stub: Stub? = Stub()

        assertWithMessage(TEST_MESSAGE).that(stub).withStrategy(testStrategy).isNull()
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(stub!!).withStrategy(testStrategy).isNotInstanceOf<Stub>()
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)

        assertWithMessage(TEST_MESSAGE).that(false).withStrategy(testStrategy).isTrue()
        testStrategy.verifyFailureAndClear(TEST_MESSAGE)
    }
}