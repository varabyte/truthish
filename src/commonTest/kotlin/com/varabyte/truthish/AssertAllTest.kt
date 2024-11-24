package com.varabyte.truthish

import com.varabyte.truthish.failure.*
import kotlin.test.Test

class AssertAllTest {
    @Test
    fun assertAllCollectsMultipleErrors() {
        val a = "A"
        val b = "B"
        val c = "C"

        assertThrows<MultipleReportsError> {
            assertAll("Just an assertAll test") {
                that(a).isEqualTo(a) // ✅
                that(a).isEqualTo(b) // ❌
                that(b).isEqualTo(b) // ✅
                that(b).isEqualTo(c) // ❌
                that(c).isEqualTo(a) // ❌
                that(c).isEqualTo(c) // ✅
                that(a).isEqualTo(c) // ❌
            }
        }.let { e ->
            assertAll {
                that(e.summary).isEqualTo("Just an assertAll test")
                that(e.reports.size).isEqualTo(4)
                // Failure 1: "that(a).isEqualTo(b)"
                with(e.reports[0].details) {
                    that(this.find(DetailsFor.EXPECTED)!!).isEqualTo(b)
                    that(this.find(DetailsFor.BUT_WAS)!!).isEqualTo(a)
                }
                // Failure 4: "that(a).isEqualTo(c)"
                with(e.reports[3].details) {
                    that(this.find(DetailsFor.EXPECTED)!!).isEqualTo(c)
                    that(this.find(DetailsFor.BUT_WAS)!!).isEqualTo(a)
                }
            }
        }
    }

    @Test
    fun assertAllWithMessageWorks() {
        assertThrows<MultipleReportsError> {
            val a = "A"
            val b = "B"
            val c = "C"

            assertAll {
                withMessage("A is equal to B").that(a).isEqualTo(b)
                withMessage("B is equal to C").that(b).isEqualTo(c)
            }
        }.let { e ->
            assertAll {
                that(e.summary).isNull()
                that(e.reports.size).isEqualTo(2)
                // that(a).isEqualTo(b)
                assertThat(e.reports[0].assertSubstrings("A is equal to B"))
                assertThat(e.reports[1].assertSubstrings("B is equal to C"))
            }
        }
    }

    class DummyComparable(private val value: Int) : Comparable<DummyComparable> {
        override fun compareTo(other: DummyComparable) = value.compareTo(other.value)
    }

    @Test
    fun assertAllTestForCoverage() {
        assertAll {
            that(null).isNull()
            that(Any()).isNotSameAs(Any())
            that(DummyComparable(10)).isGreaterThan(DummyComparable(9))
            that(true).isTrue()
            that(100.toByte()).isEqualTo(100.toByte())
            that(100.toShort()).isEqualTo(100.toShort())
            that(100).isEqualTo(100)
            that(100L).isEqualTo(100L)
            that(100.0f).isEqualTo(100.0f)
            that(100.0).isEqualTo(100.0)
            that("Hello").isEqualTo("Hello")
            that(listOf(1, 2, 3)).isEqualTo(listOf(1, 2, 3))
            that(mapOf(1 to 2, 3 to 4)).isEqualTo(mapOf(1 to 2, 3 to 4))
            that(sequenceOf(1, 2, 3)).containsAllIn(1, 2, 3)
            that(arrayOf(1, 2, 3)).isEqualTo(arrayOf(1, 2, 3))
            that(booleanArrayOf(true, false)).isEqualTo(booleanArrayOf(true, false))
            that(byteArrayOf(1, 2, 3)).isEqualTo(byteArrayOf(1, 2, 3))
            that(charArrayOf('a', 'b', 'c')).isEqualTo(charArrayOf('a', 'b', 'c'))
            that(shortArrayOf(1, 2, 3)).isEqualTo(shortArrayOf(1, 2, 3))
            that(intArrayOf(1, 2, 3)).isEqualTo(intArrayOf(1, 2, 3))
            that(longArrayOf(1, 2, 3)).isEqualTo(longArrayOf(1, 2, 3))
            that(floatArrayOf(1.0f, 2.0f, 3.0f)).isEqualTo(floatArrayOf(1.0f, 2.0f, 3.0f))
            that(doubleArrayOf(1.0, 2.0, 3.0)).isEqualTo(doubleArrayOf(1.0, 2.0, 3.0))
        }

        assertThrows<MultipleReportsError> {
            assertAll {
                // Dummy "inOrder" asserters are provided from a different code branch if the first part of the
                // assert fails early.
                that(arrayOf(1, 2, 3)).containsAllIn(4, 5, 6).inOrder()
                that(arrayOf(1, 2, 3)).containsExactly(1, 2, 3, 4).inOrder()
            }
        }
    }

    @Test
    fun assertAllCallstacksAreCorrect() {
        // This test can be useful if we ever refactor code and don't realize we broke our logic for determining how
        // callstacks are collected.

        val a = "A"
        val b = "B"
        val c = "C"

        assertThrows<MultipleReportsError> {
            assertAll {
                that(a).isEqualTo(a) // ✅
                that(a).isEqualTo(b) // ❌   // First failure, line0
                that(b).isEqualTo(b) // ✅
                that(b).isEqualTo(c) // ❌   // Second failure, line0 + 2
                that(c).isEqualTo(a) // ❌   // Third failure, line0 + 3
                that(c).isEqualTo(c) // ✅
                that(a).isEqualTo(c) // ❌   // Fourth failure, line0 + 5
            }
        }.let { e ->
            // Use a regex to extract callstack values, so that this test will still pass even if the line numbers change
            // Example callstack entry: com.varabyte.truthish.AssertAllTest$assertAllCallstacksAreCorrect$2$1.invoke(AssertAllTest.kt:123)
            val expectedAt = Regex(
                """com\.varabyte\.truthish\.AssertAllTest${"\\$"}assertAllCallstacksAreCorrect${"\\$"}(\d+)${"\\$"}(\d+)\.invoke\(AssertAllTest\.kt:(\d+)\)"""
            )
            val match = expectedAt.matchEntire(e.reports[0].details.find(DetailsFor.AT).toString())!!
            val outerLambdaId = match.groupValues[1].toInt() // assertThrows
            val innerLambdaId = match.groupValues[2].toInt() // assertAll
            val lineNumber = match.groupValues[3].toInt()

            assertAll {
                // "Error report" to "delta distance from the first report"
                val reportsToCheck = listOf(
                    e.reports[1] to 2,
                    e.reports[2] to 3,
                    e.reports[3] to 5
                )

                reportsToCheck.forEach { (report, lineDelta) ->
                    with(expectedAt.matchEntire(report.details.find(DetailsFor.AT).toString())!!) {
                        that(groupValues[1].toInt()).isEqualTo(outerLambdaId)
                        that(groupValues[2].toInt()).isEqualTo(innerLambdaId)
                        that(groupValues[3].toInt()).isEqualTo(lineNumber + lineDelta)
                    }
                }
            }
        }
    }

}
