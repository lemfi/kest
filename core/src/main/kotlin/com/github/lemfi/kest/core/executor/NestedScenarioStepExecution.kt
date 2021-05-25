package com.github.lemfi.kest.core.executor

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.ExecutionDescription
import com.github.lemfi.kest.core.model.NestedScenario

class NestedScenarioStepExecution<T>(
    val scenario: () -> NestedScenario<T>,
) : Execution<T>() {
    override var description: ExecutionDescription? = null

    override fun execute(): T =
        scenario()
            .apply {
                this@NestedScenarioStepExecution.description = ExecutionDescription(this.name.name)
            }
            .let {
                it.run()
                it.result()
            }
}
