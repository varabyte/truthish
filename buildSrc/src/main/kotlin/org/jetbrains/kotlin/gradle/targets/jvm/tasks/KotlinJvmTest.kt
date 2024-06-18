package org.jetbrains.kotlin.gradle.targets.jvm.tasks

import org.gradle.api.internal.tasks.testing.JvmTestExecutionSpec
import org.gradle.api.internal.tasks.testing.TestDescriptorInternal
import org.gradle.api.internal.tasks.testing.TestExecuter
import org.gradle.api.internal.tasks.testing.TestResultProcessor
import org.gradle.api.internal.tasks.testing.TestStartEvent
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.testing.Test

// See: https://youtrack.jetbrains.com/issue/KT-54634/MPP-Test-Failure-causes-KotlinJvmTestExecutorexecute1-does-not-define-failure#focus=Comments-27-6691278.0-0
@CacheableTask
open class KotlinJvmTest : Test() {
    @Input
    @Optional
    var targetName: String? = null

    private lateinit var _dryRun: Property<Boolean>
    override fun getDryRun(): Property<Boolean> {
        if (!::_dryRun.isInitialized) {
            _dryRun = objectFactory.property(Boolean::class.java)
        }
        return _dryRun
    }

    override fun createTestExecuter(): TestExecuter<JvmTestExecutionSpec> =
        if (targetName != null) Executor(
            super.createTestExecuter(),
            targetName!!
        )
        else super.createTestExecuter()

    class Executor(
        private val delegate: TestExecuter<JvmTestExecutionSpec>,
        private val targetName: String
    ) : TestExecuter<JvmTestExecutionSpec> by delegate {
        override fun execute(testExecutionSpec: JvmTestExecutionSpec, testResultProcessor: TestResultProcessor) {
            delegate.execute(testExecutionSpec, object : TestResultProcessor by testResultProcessor {
                override fun started(test: TestDescriptorInternal, event: TestStartEvent) {
                    val myTest = object : TestDescriptorInternal by test {
                        override fun getDisplayName(): String = "${test.displayName}[$targetName]"
                        override fun getClassName(): String? = test.className?.replace('$', '.')
                        override fun getClassDisplayName(): String? = test.classDisplayName?.replace('$', '.')
                    }
                    testResultProcessor.started(myTest, event)
                }
            })
        }
    }
}
