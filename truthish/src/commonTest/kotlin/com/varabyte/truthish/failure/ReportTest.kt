package com.varabyte.truthish.failure

import com.varabyte.truthish.assertThat
import kotlin.test.Test

class ReportTest {
    @Test
    fun reportWithoutDetailsIsJustTheSummary() {
        assertThat(Report("Summary").toString()).isEqualTo("Summary\n")
    }

    @Test
    fun detailLabelsAreAligned() {
        val report = Report("Summary", Details(listOf("A" to 1, "Longer" to 2)))
        assertThat(report.toString()).isEqualTo(
            """
            Summary

            A     : 1
            Longer: 2

            """.trimIndent()
        )
    }

    @Test
    fun detailValuesAreFormattedByType() {
        val report = Report(
            "Summary",
            Details(
                listOf(
                    "String" to "x",
                    "Char" to 'c',
                    "Null" to null,
                    "List" to listOf(1, 2),
                    "Set" to setOf(1, 2),
                    "Map" to mapOf("a" to 1),
                    "Empty list" to emptyList<Int>(),
                    "Empty set" to emptySet<Int>(),
                    "Empty map" to emptyMap<String, Int>(),
                )
            )
        )
        assertThat(report.toString()).isEqualTo(
            """
            Summary

            String    : "x"
            Char      : 'c'
            Null      : (null)
            List      : [ 1, 2 ]
            Set       : { 1, 2 }
            Map       : { a=1 }
            Empty list: []
            Empty set : {}
            Empty map : {}

            """.trimIndent()
        )
    }

    @Test
    fun detailsCanBeAdded() {
        val details = Details(listOf("Middle" to 2))
        details.add("Last" to 3)
        details.add(0, "First" to 1)

        assertThat(Report("Summary", details).toString()).isEqualTo(
            """
            Summary

            First : 1
            Middle: 2
            Last  : 3

            """.trimIndent()
        )
    }

    @Test
    fun findReturnsRawValues() {
        val details = Details(listOf("Number" to 1, "List" to listOf("a")))

        assertThat(details.find("Number")).isEqualTo(1)
        assertThat(details.find("List")).isEqualTo(listOf("a"))
        assertThat(details.find("Missing")).isNull()
    }

    @Test
    fun detailsForHelpersUseStandardLabels() {
        assertThat(Report("Summary", DetailsFor.actual(1)).toString()).isEqualTo(
            """
            Summary

            Value: 1

            """.trimIndent()
        )

        assertThat(Report("Summary", DetailsFor.expected(1)).toString()).isEqualTo(
            """
            Summary

            Expected: 1

            """.trimIndent()
        )

        assertThat(Report("Summary", DetailsFor.expectedActual(1, 2)).toString()).isEqualTo(
            """
            Summary

            Expected: 1
            But was : 2

            """.trimIndent()
        )

        assertThat(Report("Summary", DetailsFor.expectedActual("greater than", 2, 1)).toString()).isEqualTo(
            """
            Summary

            Expected greater than: 2
            But was              : 1

            """.trimIndent()
        )
    }

    @Test
    fun ambiguousValuesShowTypes() {
        class ClassA(private val value: String) {
            override fun toString() = value
        }

        class ClassB(private val value: String) {
            override fun toString() = value
        }

        assertThat(Report("Summary", DetailsFor.expectedActual(ClassB("x"), ClassA("x"))).toString()).isEqualTo(
            """
            Summary

            Expected: x (Type: `ClassB`)
            But was : x (Type: `ClassA`)

            """.trimIndent()
        )

        assertThat(Report("Summary", DetailsFor.expectedActual("similar to", ClassB("x"), ClassA("x"))).toString()).isEqualTo(
            """
            Summary

            Expected similar to: x (Type: `ClassB`)
            But was            : x (Type: `ClassA`)

            """.trimIndent()
        )
    }
}
