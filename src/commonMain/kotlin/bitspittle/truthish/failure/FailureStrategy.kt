package bitspittle.truthish.failure

/**
 * A strategy for how an assertion failure should be handled.
 *
 * The most common strategy is to throw an exception, but a custom strategy can be registered with
 * a [Reportable] if needed.
 */
interface FailureStrategy {
    fun handle(report: Report)
}

/**
 * A strategy that will cause the test to fail immediately.
 */
class AssertionStrategy : FailureStrategy {
    override fun handle(report: Report) {
        throw AssertionError(report.toString())
    }
}
