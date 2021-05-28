package com.github.lemfi.kest.core.executor

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.model.*

class NestedScenarioStepExecution<T>(
    val step: Step<T>,
    val scenario: () -> NestedScenario<T>,
) : Execution<T>() {
    override var description: ExecutionDescription? = null

    override fun execute(): T =
        scenario()
            .apply {
                this@NestedScenarioStepExecution.description = ExecutionDescription(this.name.value)
            }
            .let {
                it.run()
                it.result()
                    .apply { step.postExecution.setResult(this) }
            }
}
