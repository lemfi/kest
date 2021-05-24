package com.github.lemfi.kest.core.executor

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.NestedScenario
import com.github.lemfi.kest.core.model.StepName

class NestedScenarioStepExecution<T>(
    val scenario: () -> NestedScenario<T>,
) : Execution<T>() {
    override var name: StepName? = null

    override fun execute(): T =
        scenario()
            .apply {
                this@NestedScenarioStepExecution.name = StepName(this.name.name)
            }
            .let {
                it.run()
                it.result()
            }
}
