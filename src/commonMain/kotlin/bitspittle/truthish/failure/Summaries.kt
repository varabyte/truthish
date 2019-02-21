package bitspittle.truthish.failure

/**
 * Re-usable summaries for all subjects
 */
object Summaries {
    const val EXPECTED_NULL = "An object was not null"
    const val EXPECTED_NOT_NULL = "An object was null"
    const val EXPECTED_EQUAL = "Two objects were not equal"
    const val EXPECTED_NOT_EQUAL = "Two objects were equal"
    const val EXPECTED_SAME = "Two objects were not the same"
    const val EXPECTED_NOT_SAME = "Two objects were the same"
    const val EXPECTED_INSTANCE = "An object was not an instance of a class"
    const val EXPECTED_NOT_INSTANCE = "An object was an instance of a class"

    const val EXPECTED_TRUE = "A value was false"
    const val EXPECTED_FALSE = "A value was true"

    const val EXPECTED_EXCEPTION = "An exception was not thrown"

    const val EXPECTED_COMPARISON = "Two values did not compare with each other as expected"

    const val EXPECTED_EMPTY = "A value was not empty"
    const val EXPECTED_BLANK = "A value was not blank"
    const val EXPECTED_NOT_EMPTY = "A value was empty"
    const val EXPECTED_NOT_BLANK = "A value was blank"

    const val EXPECTED_STARTS_WITH = "A value did not start with another"
    const val EXPECTED_NOT_STARTS_WITH = "A value started with another"
    const val EXPECTED_ENDS_WITH = "A value did not end with another"
    const val EXPECTED_NOT_ENDS_WITH = "A value ended with another"
    const val EXPECTED_CONTAINS = "A value did not contain another"
    const val EXPECTED_NOT_CONTAINS = "A value contained another"
    const val EXPECTED_MATCH = "A value did not match another"
    const val EXPECTED_NOT_MATCH = "A value matched another"
}
