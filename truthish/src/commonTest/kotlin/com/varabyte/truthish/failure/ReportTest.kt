package com.varabyte.truthish.failure

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.util.truncate.Anchor
import com.varabyte.truthish.util.truncate.Truncation
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
                    "Pair" to ('a' to 1),
                    "Nested pair" to ((1 to 2) to (3 to 4)),
                    "Triple" to Triple("a", 'b', 3),
                    "Empty list" to emptyList<Int>(),
                    "Empty set" to emptySet<Int>(),
                    "Empty map" to emptyMap<String, Int>(),
                )
            )
        )
        assertThat(report.toString()).isEqualTo(
            """
            Summary

            String     : "x"
            Char       : 'c'
            Null       : (null)
            List       : [ 1, 2 ]
            Set        : { 1, 2 }
            Map        : { "a" to 1 }
            Pair       : 'a' to 1
            Nested pair: (1 to 2) to (3 to 4)
            Triple     : ("a", 'b', 3)
            Empty list : []
            Empty set  : {}
            Empty map  : {}

            """.trimIndent()
        )
    }

    @Test
    fun specialCharactersAreEscaped() {
        val report = Report(
            "Summary",
            Details(
                listOf(
                    "Whitespace" to "a\tb\nc\r",
                    "Quotes" to "\"x\" 'y'",
                    "Backslash" to "a\\b",
                    "ANSI" to "\u001B[31mred\u001B[0m",
                    "Char quote" to '\'',
                    "Char tab" to '\t',
                )
            )
        )
        assertThat(report.toString()).isEqualTo(
            """
            Summary

            Whitespace: "a\tb\nc\r"
            Quotes    : "\"x\" 'y'"
            Backslash : "a\\b"
            ANSI      : "\u001B[31mred\u001B[0m"
            Char quote: '\''
            Char tab  : '\t'

            """.trimIndent()
        )
    }

    @Test
    fun multiLineValuesLineUpWithTheValueColumn() {
        // Strings are escaped onto one line, so pass text through as is, the way messages are
        val report = Report(
            "Summary",
            Details(
                listOf(
                    "Title" to AnyStringifier("Release notes"),
                    "Authors" to AnyStringifier("Ann\nBob"),
                    "Body" to AnyStringifier("Fixed a crash.\n\nThanks for reporting it!"),
                ),
                extras = mapOf("Authors" to "2 total"),
            )
        )
        assertThat(report.toString()).isEqualTo(
            """
            Summary

            Title  : Release notes
            Authors: Ann
                     Bob
                     (2 total)
            Body   : Fixed a crash.

                     Thanks for reporting it!

            """.trimIndent()
        )
    }

    @Test
    fun longCollectionsAreTruncated() {
        val report = Report(
            "Summary",
            Details(
                listOf(
                    "Eleven" to (1..11).toList(),
                    "Twelve" to (1..12).toList(),
                    "Set" to (1..20).toSet(),
                    "Map" to (1..13).associateWith { it },
                    "Nested" to listOf((1..13).toList()),
                )
            )
        )
        assertThat(report.toString()).isEqualTo(
            """
            Summary

            Eleven: [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ]
            Twelve: [ 1, 2, 3, 4, 5, 6, … (1 more), 8, 9, 10, 11, 12 ]
            Set   : { 1, 2, 3, 4, 5, 6, … (9 more), 16, 17, 18, 19, 20 }
            Map   : { 1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 5, 6 to 6, … (2 more), 9 to 9, 10 to 10, 11 to 11, 12 to 12, 13 to 13 }
            Nested: [ [ 1, 2, 3, 4, 5, 6, … (2 more), 9, 10, 11, 12, 13 ] ]

            """.trimIndent()
        )
    }

    @Test
    fun longStringsAreTruncated() {
        val numbers = (1..100).joinToString(",")
        val details = Details(
            listOf(
                "Hundred" to numbers.take(100), // Max allowed before truncation
                "All" to numbers,
                "Escapes" to "\n".repeat(101),
                "Nested" to listOf(numbers),
            )
        )
        val fiftyIndex = numbers.indexOf(",50,") + 1
        details.add("Anchored" to numbers, StringifierContext(Truncation.Anchored(Anchor.CenteredOn(fiftyIndex))))
        details.add("Full" to numbers, StringifierContext(Truncation.None))

        assertThat(Report("Summary", details).toString()).isEqualTo(
            """
            Summary

            Hundred : "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,3"
            All     : "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20" … (191 more chars) "4,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100"
            Escapes : "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" … (1 more char) "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
            Nested  : [ "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20" … (191 more chars) "4,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100" ]
            Anchored: … (88 more chars) "3,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66" … (103 more chars)
            Full    : "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100"

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
    fun addedDetailsCanSetHowTheyAreRendered() {
        val details = Details(listOf("Middle" to (1..20).toList()))
        details.add("Last" to (1..20).toList(), StringifierContext(Truncation.None))
        details.add(0, "First" to (1..20).toList(), StringifierContext(Truncation.Anchored(Anchor.CenteredOn(9))))

        assertThat(Report("Summary", details).toString()).isEqualTo(
            """
            Summary

            First : [ … (4 more), 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, … (5 more) ]
            Middle: [ 1, 2, 3, 4, 5, 6, … (9 more), 16, 17, 18, 19, 20 ]
            Last  : [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 ]

            """.trimIndent()
        )
    }

    @Test
    fun expectedActualCanSetHowValuesAreRendered() {
        val details = DetailsFor.expectedActual(
            "to contain",
            (1..20).toList(),
            listOf((1..20).toList()),
            expectedContext = StringifierContext(Truncation.None),
        )

        assertThat(Report("Summary", details).toString()).isEqualTo(
            """
            Summary

            Expected to contain: [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 ]
            But was            : [ [ 1, 2, 3, 4, 5, 6, … (9 more), 16, 17, 18, 19, 20 ] ]

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
