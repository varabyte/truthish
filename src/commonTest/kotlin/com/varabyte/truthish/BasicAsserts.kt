package com.varabyte.truthish

import com.varabyte.truthish.failure.*
import com.varabyte.truthish.failure.ReportError
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
            assertThrows<ReportError> {
                assertThat("str").isNotEqualTo("str")
            }.assertSubstrings(Summaries.EXPECTED_NOT_EQUAL, "str")
            assertThrows<ReportError> {
                assertThat("str1").isEqualTo("str2")
            }.assertSubstrings(Summaries.EXPECTED_EQUAL, "str1", "str2")
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
            var stub: Stub? = Stub()
            assertThrows<ReportError> {
                assertThat(stub).isNull()
            }.assertSubstrings(Summaries.EXPECTED_NULL)

            stub = null
            assertThrows<ReportError> {
                assertThat(stub).isNotNull()
            }.assertSubstrings(Summaries.EXPECTED_NOT_NULL)
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
            assertThrows<ReportError> {
                assertThat(IntValue(234)).isInstanceOf<Int>()
            }.assertSubstrings(Summaries.EXPECTED_INSTANCE, "IntValue", "Int")

            assertThrows<ReportError> {
                assertThat(IntValue(234)).isNotInstanceOf<IntValue>()
            }.assertSubstrings(Summaries.EXPECTED_NOT_INSTANCE, "IntValue")
        }
    }

    @Test
    fun assertSame() {
        run {
            // Test true statements
            val stubValue1 = Stub()
            val stubValue2 = Stub()
            val stubValue3 = stubValue1

            assertThat(stubValue1).isSameAs(stubValue1)
            assertThat(stubValue1).isNotSameAs(stubValue2)
            assertThat(stubValue1).isSameAs(stubValue3)

        }

        run {
            // Test false statements
            val stubValue1 = Stub()
            val stubValue2 = Stub()
            val stubValue3 = stubValue1

            assertThrows<ReportError> {
                assertThat(stubValue1).isSameAs(stubValue2)
            }.assertSubstrings(Summaries.EXPECTED_SAME)

            assertThrows<ReportError> {
                assertThat(stubValue1).isNotSameAs(stubValue3)
            }.assertSubstrings(Summaries.EXPECTED_NOT_SAME)
        }
    }

    @Test
    fun assertNamed() {
        val stub: Stub? = Stub()
        assertThrows<ReportError> {
            assertThat(stub).named("Stubby McStubberson").isNull()
        }.assertSubstrings("Stubby McStubberson")
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
                // This is the real assert test. The outer one captures it in order to verify the failure text.
                assertThrows<IllegalArgumentException> {
                }
            }
            assertThat(e.message!!).contains(Summaries.EXPECTED_EXCEPTION)
            assertThat(e.message!!).contains("IllegalArgumentException")
        }

        run { // assertThrows doesn't accept invalid exceptions.
            val e = assertThrows<AssertionError> {
                // This is the real assert test. The outer one captures it in order to verify the failure text.
                assertThrows<IllegalArgumentException> {
                    throw IllegalStateException()
                }
            }
            assertThat(e.message!!).contains(Summaries.EXPECTED_EXCEPTION)
            assertThat(e.message!!).contains("IllegalArgumentException")
            assertThat(e.message!!).contains("IllegalStateException")
        }
    }

    @Test
    fun assertThrowsWithMessage() {
        run { // Verify the correct path
            val e = assertThrows<IllegalArgumentException>(message = "not used") {
                throw IllegalArgumentException("xyz")
            }
            assertThat(e.message).isEqualTo("xyz")
        }

        run { // assertThrows doesn't accept no exceptions.
            val e = assertThrows<AssertionError>(message = "outer assert") {
                // This is the real assert test. The outer one captures it in order to verify the failure text.
                assertThrows<IllegalArgumentException>(message = "inner assert") {
                }
            }
            assertThat(e.message!!).contains(Summaries.EXPECTED_EXCEPTION)
            assertThat(e.message!!).contains("IllegalArgumentException")
            assertThat(e.message!!).contains("inner assert")
            assertThat(e.message!!).doesNotContain("outer assert")
        }

        run { // assertThrows doesn't accept invalid exceptions.
            val e = assertThrows<AssertionError>(message = "outer assert") {
                // This is the real assert test. The outer one captures it in order to verify the failure text.
                assertThrows<IllegalArgumentException>(message = "inner assert") {
                    throw IllegalStateException()
                }
            }
            assertThat(e.message!!).contains(Summaries.EXPECTED_EXCEPTION)
            assertThat(e.message!!).contains("IllegalArgumentException")
            assertThat(e.message!!).contains("IllegalStateException")
            assertThat(e.message!!).contains("inner assert")
            assertThat(e.message!!).doesNotContain("outer assert")
        }
    }
}