package com.github.bitspittle.truthish.failure

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

inline fun <reified R : Reportable> R.named(name: String): R {
    this.name = name
    return this
}

inline fun <reified R : Reportable> R.withMessage(message: String): R {
    this.message = message
    return this
}

inline fun <reified R : Reportable> R.withStrategy(strategy: FailureStrategy): R {
    this.strategy = strategy
    return this
}
