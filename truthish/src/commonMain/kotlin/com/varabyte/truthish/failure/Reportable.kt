package com.varabyte.truthish.failure

import com.varabyte.truthish.assertWithMessage

/**
 * Base class for any object that may want to send a failure report.
 */
abstract class Reportable {
    var strategy: FailureStrategy = AssertionStrategy()
    var message: String? = null
    var name: String? = null

    /**
     * Handle a [Report] using this current reportable instance's strategy and other values.
     */
    fun report(report: Report) {
        // Note: The following lines are in reverse order, since each add to the 0th element. In
        // practice, it's rare that both will even be set at the same time.
        name?.let { report.details.add(0, "Name" to AnyStringifier(it)) }
        message?.let { report.details.add(0, "Message" to AnyStringifier(it)) }

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
 *    assertThat(voter.age).named("Age of ${voter.name}").isGreaterThanOrEqualTo(18)
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
