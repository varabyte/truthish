package com.varabyte.truthish.failure

import com.varabyte.truthish.assertWithMessage

/**
 * Adds a custom message, provided by the user when making an assertion, to these details.
 *
 * The message is inserted first, since the user wrote it to explain the failure, so it's usually the most important
 * detail.
 */
@PublishedApi
internal fun Details.addMessage(message: String) {
    add(0, DetailsFor.MESSAGE to AnyStringifier(message))
}

/**
 * Base class for any object that may want to send a failure report.
 */
abstract class Reportable {
    var strategy: FailureStrategy = AssertionStrategy()
    var message: String? = null
    var name: String? = null
    /** Context about where this value comes from. */
    var valueOf: String? = null

    /**
     * Handle a [Report] using this current reportable instance's strategy and other values.
     */
    fun report(report: Report) {
        // Note: The following lines are in reverse order, since each add to the 0th element
        valueOf?.let { report.details.add(0, DetailsFor.VALUE_OF to AnyStringifier(it)) }
        name?.let { report.details.add(0, DetailsFor.NAME to AnyStringifier(it)) }
        message?.let { report.details.addMessage(it) }

        strategy.handle(report)
    }
}

/**
 * Adds a name to a value, which can be useful for providing more context to a failure.
 *
 * For example:
 *
 * ```
 * val voters = personDb.queryVoters()
 * for (voter in voters) {
 *    assertThat(voter.age).named("Age of ${voter.name}").isAtLeast(18)
 * }
 * ```
 *
 * This is very similar to [withMessage], but there is a subtle difference. An assertion's message should describe the
 * overall check itself, while the name should describe the value being checked. It can even make sense to set both
 * values.
 *
 * @see withMessage
 */
inline fun <reified R : Reportable> R.named(name: String): R {
    this.name = name
    return this
}

/**
 * Adds a message that describes the assertion, which can be useful for providing more context to a failure.
 *
 * @see assertWithMessage
 * @see named
 */
inline fun <reified R : Reportable> R.withMessage(message: String): R {
    this.message = message
    return this
}

/**
 * Overrides the [FailureStrategy] used by the assertion.
 *
 * Most users will never need to use this, but if you need custom [Report] handling that doesn't simply throw an
 * [AssertionError] on failure, this is how you can override the default behavior.
 */
inline fun <reified R : Reportable> R.withStrategy(strategy: FailureStrategy): R {
    this.strategy = strategy
    return this
}

/**
 * Adds context about where a value comes from, e.g. `ParseException.message` for an exception's message.
 *
 * This is especially useful to tell a derived subject apart from its source, and we expect it to commonly follow a
 * call to [derivedFrom].
 */
fun <R : Reportable> R.valueOf(valueOf: String): R {
    this.valueOf = valueOf
    return this
}

/**
 * Copies [other]'s message, name and failure strategy, so this subject reports failures like it would.
 *
 * This pattern is useful if you are exposing a child subject from a parent subject. For example, imagine you created a
 * custom `UserSubject` and wanted to return a child `StringSubject` representing the user's name. You could write:
 * ```
 * fun hasNameThat() = StringSubject(user.name).derivedFrom(this).valueOf("User.name")
 * ```
 */
fun <R : Reportable> R.derivedFrom(other: Reportable): R {
    name = other.name
    message = other.message
    strategy = other.strategy
    return this
}
