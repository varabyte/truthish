package com.varabyte.truthish

import com.varabyte.truthish.failure.DetailsFor
import com.varabyte.truthish.failure.MultipleReportsError
import com.varabyte.truthish.failure.Summaries
import com.varabyte.truthish.failure.assertSubstrings
import com.varabyte.truthish.failure.derivedFrom
import com.varabyte.truthish.failure.valueOf
import com.varabyte.truthish.subjects.NotNullSubject
import com.varabyte.truthish.subjects.StringSubject
import kotlin.test.Test

class CustomSubjectAsserts {
    class Person(val name: String)

    class PersonSubject(private val person: Person) : NotNullSubject<Person>(person) {
        fun hasName(name: String) = StringSubject(person.name).derivedFrom(this).valueOf("Person.name").isEqualTo(name)
    }

    private fun AssertionEntryPoint.that(person: Person) = prepare(PersonSubject(person))
    private fun assertThat(person: Person) = AssertionEntryPoint.that(person)

    private val alice = Person("Alice")
    private val bob = Person("Bob")

    @Test
    fun customSubjectInAssertThat() {
        assertThat(alice).hasName("Alice")

        assertThrows<AssertionError> {
            assertThat(alice).hasName("Bob")
        }.assertSubstrings(Summaries.EXPECTED_EQUAL, DetailsFor.VALUE_OF, "Person.name")
    }

    @Test
    fun customSubjectInAssertAll() {
        assertAll {
            that(alice).hasName("Alice")
            that(bob).hasName("Bob")
        }

        assertThrows<MultipleReportsError> {
            assertAll {
                that(alice).hasName("Bob")
                that(bob).hasName("Alice")
            }
        }.let { e ->
            assertThat(e.reports.size).isEqualTo(2)
            e.assertSubstrings(
                Summaries.EXPECTED_EQUAL, DetailsFor.VALUE_OF, "Person.name", "\"Bob\"", "\"Alice\"",
                Summaries.EXPECTED_EQUAL, DetailsFor.VALUE_OF, "Person.name", "\"Alice\"", "\"Bob\"",
            )
        }
    }

    @Test
    fun customSubjectWithMessage() {
        assertWithMessage(TEST_MESSAGE).that(alice).hasName("Alice")

        assertThrows<AssertionError> {
            assertWithMessage(TEST_MESSAGE).that(alice).hasName("Bob")
        }.assertSubstrings(Summaries.EXPECTED_EQUAL, DetailsFor.MESSAGE, TEST_MESSAGE, DetailsFor.VALUE_OF, "Person.name")

        assertThrows<MultipleReportsError> {
            assertAll {
                withMessage(TEST_MESSAGE).that(alice).hasName("Bob")
                that(bob).hasName("Alice")
            }
        }.let { e ->
            assertThat(e.reports.size).isEqualTo(2)
            e.assertSubstrings(DetailsFor.MESSAGE, TEST_MESSAGE, DetailsFor.VALUE_OF, "Person.name")
        }
    }
}
