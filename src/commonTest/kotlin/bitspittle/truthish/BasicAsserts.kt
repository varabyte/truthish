package bitspittle.truthish

import bitspittle.truthish.failure.Summaries
import bitspittle.truthish.failure.named
import bitspittle.truthish.failure.withStrategy
import kotlin.test.Test

class BasicAsserts {
    data class IntValue(val value: Int) : Comparable<IntValue> {
        override fun compareTo(other: IntValue) = value.compareTo(other.value)
    }

    class Stub

    @Test
    fun assertEquality() {
        run {
            // Test true statements
            assertThat("str").isEqualTo("str")
            assertThat("str1").isNotEqualTo("str2")

            assertThat(IntValue(10)).isEqualTo(IntValue(10))
            assertThat(IntValue(10)).isNotEqualTo(IntValue(11))
        }

        run {
            // Test false statements
            val testStrategy = TestStrategy()

            assertThat("str").withStrategy(testStrategy).isNotEqualTo("str")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_EQUAL, "str")
            assertThat("str1").withStrategy(testStrategy).isEqualTo("str2")
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_EQUAL, "str1", "str2")
        }
    }

    @Test
    fun assertNullity() {
        run {
            // Test true statements
            var stub: Stub? = Stub()
            assertThat(stub).isNotNull()

            stub = null
            assertThat(stub).isNull()
        }

        run {
            // Test false statements
            val testStrategy = TestStrategy()
            var stub: Stub? = Stub()
            assertThat(stub).withStrategy(testStrategy).isNull()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NULL)

            stub = null
            assertThat(stub).withStrategy(testStrategy).isNotNull()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_NULL)
        }
    }

    @Test
    fun assertInstance() {
        run {
            // Test true statements
            assertThat(IntValue(234)).isInstanceOf<IntValue>()
            assertThat(IntValue(456)).isNotInstanceOf<Int>()
            assertThat(IntValue(789)).isInstanceOf<Any>()
        }

        run {
            // Test false statements
            val testStrategy = TestStrategy()

            assertThat(IntValue(234)).withStrategy(testStrategy).isInstanceOf<Int>()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_INSTANCE, "IntValue", "Int")

            assertThat(IntValue(234)).withStrategy(testStrategy).isNotInstanceOf<IntValue>()
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_INSTANCE, "IntValue")
        }
    }

    @Test
    fun assertSame() {
        run {
            // Test true statements
            val intValue1 = IntValue(123)
            val intValue2 = IntValue(123)
            val intValue3 = intValue1

            assertThat(intValue1).isSameAs(intValue3)
            assertThat(intValue1).isNotSameAs(intValue2)
        }

        run {
            // Test false statements
            val testStrategy = TestStrategy()

            val intValue1 = IntValue(123)
            val intValue2 = IntValue(123)
            val intValue3 = intValue1

            assertThat(intValue1).withStrategy(testStrategy).isSameAs(intValue2)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_SAME)

            assertThat(intValue1).withStrategy(testStrategy).isNotSameAs(intValue3)
            testStrategy.verifyFailureAndClear(Summaries.EXPECTED_NOT_SAME)
        }
    }

    @Test
    fun assertNamed() {
        val testStrategy = TestStrategy()
        val stub: Stub? = Stub()
        assertThat(stub).named("Stubby McStubberson").withStrategy(testStrategy).isNull()
        testStrategy.verifyFailureAndClear("Stubby McStubberson")
    }

    @Test
    fun assertThrows() {
        run { // Verify the correct path
            val e = assertThrows<IllegalArgumentException> {
                throw IllegalArgumentException("xyz")
            }
            assertThat(e.message).isEqualTo("xyz")
        }

        run { // assertThrows doesn't accept no exceptions.
            val e = assertThrows<AssertionError> {
                // Outer block captures inner-block, so the test can keep going
                assertThrows<IllegalArgumentException> {
                }
            }
            assertThat(e.message!!.contains(Summaries.EXPECTED_EXCEPTION)).isTrue()
            assertThat(e.message!!.contains("IllegalArgumentException")).isTrue()
        }

        run { // assertThrows doesn't accept invalid exceptions.
            val e = assertThrows<AssertionError> {
                // Outer block captures inner-block, so the test can keep going
                assertThrows<IllegalArgumentException> {
                    throw IllegalStateException()
                }
            }
            assertThat(e.message!!.contains(Summaries.EXPECTED_EXCEPTION)).isTrue()
            assertThat(e.message!!.contains("IllegalArgumentException")).isTrue()
            assertThat(e.message!!.contains("IllegalStateException")).isTrue()
        }
    }
}