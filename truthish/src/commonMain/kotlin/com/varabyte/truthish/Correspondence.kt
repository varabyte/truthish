package com.varabyte.truthish

import com.varabyte.truthish.subjects.DoubleSubject
import com.varabyte.truthish.subjects.IterableSubject
import kotlin.math.absoluteValue

/**
 * A rule for whether an actual element corresponds to an expected one, for comparing the elements of a collection more
 * loosely than with `==`.
 *
 * For example, to check a list of error messages without spelling each one out in full:
 *
 * ```
 * assertThat(errors)
 *     .comparingElementsUsing(Correspondence.contains)
 *     .containsExactly("unresolved", "cannot be reassigned")
 * ```
 *
 * You can also write your own, and the actual and expected types can differ. For example, to check strings against the
 * numbers they should parse to:
 *
 * ```
 * val parsesTo = Correspondence<String, Int>("parses to") { actual, expected -> actual.toIntOrNull() == expected }
 *
 * assertThat(listOf("1", "2", "3"))
 *     .comparingElementsUsing(parsesTo)
 *     .containsExactly(1, 2, 3)
 * ```
 *
 * @param description What an actual element does to the expected element it corresponds to, e.g. "contains" or
 *   "parses to". Reports show it as "actual element contains expected element", so it should read as a verb phrase.
 * @param predicate Whether [compare]'s `actual` element corresponds to its `expected` one.
 *
 * @see IterableSubject.comparingElementsUsing
 */
class Correspondence<in A, in E>(
    val description: String,
    private val predicate: (actual: A, expected: E) -> Boolean,
) {
    companion object {
        /** Each actual string contains the expected string. */
        val contains = Correspondence<CharSequence, CharSequence>("contains") { actual, expected ->
            actual.contains(expected)
        }

        /** Like [contains], but ignoring case if [ignoreCase] is true. */
        fun contains(ignoreCase: Boolean): Correspondence<CharSequence, CharSequence> {
            if (!ignoreCase) return contains
            return Correspondence("contains (ignoring case)") { actual, expected ->
                actual.contains(expected, ignoreCase = true)
            }
        }

        /** Each actual string starts with the expected string. */
        val startsWith = Correspondence<CharSequence, CharSequence>("starts with") { actual, expected ->
            actual.startsWith(expected)
        }

        /** Like [startsWith], but ignoring case if [ignoreCase] is true. */
        fun startsWith(ignoreCase: Boolean): Correspondence<CharSequence, CharSequence> {
            if (!ignoreCase) return startsWith
            return Correspondence("starts with (ignoring case)") { actual, expected ->
                actual.startsWith(expected, ignoreCase = true)
            }
        }

        /** Each actual string ends with the expected string. */
        val endsWith = Correspondence<CharSequence, CharSequence>("ends with") { actual, expected ->
            actual.endsWith(expected)
        }

        /** Like [endsWith], but ignoring case if [ignoreCase] is true. */
        fun endsWith(ignoreCase: Boolean): Correspondence<CharSequence, CharSequence> {
            if (!ignoreCase) return endsWith
            return Correspondence("ends with (ignoring case)") { actual, expected ->
                actual.endsWith(expected, ignoreCase = true)
            }
        }

        /** Each actual string matches the expected regex in full. */
        val matches = Correspondence<CharSequence, Regex>("matches") { actual, expected -> expected.matches(actual) }

        /**
         * Each actual number is within [epsilon] of the expected number, e.g. to compare the results of floating point
         * math, which rarely land on exact values.
         *
         * Like [DoubleSubject.isWithin], NaN never corresponds to anything.
         */
        fun within(epsilon: Double): Correspondence<Number, Number> {
            require(epsilon >= 0.0) { "Epsilon must be a non-negative number, but was $epsilon" }
            return Correspondence("is within $epsilon of") { actual, expected ->
                (actual.toDouble() - expected.toDouble()).absoluteValue <= epsilon
            }
        }

        /**
         * Each actual element, after [transform], equals the expected element, e.g. to compare objects by a property.
         *
         * ```
         * assertThat(users)
         *     .comparingElementsUsing(Correspondence.transforming("is named") { user: User -> user.name })
         *     .containsExactly("Ann", "Bo")
         * ```
         */
        fun <A, E> transforming(description: String, transform: (A) -> E) =
            Correspondence<A, E>(description) { actual, expected -> transform(actual) == expected }
    }

    fun compare(actual: A, expected: E): Boolean = predicate(actual, expected)
}
