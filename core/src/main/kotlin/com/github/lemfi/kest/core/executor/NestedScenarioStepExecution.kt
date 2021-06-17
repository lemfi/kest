package com.github.lemfi.kest.core.executor

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.model.*

class NestedScenarioStepExecution<T>(
    val step: Step<T>,
    val scenario: () -> NestedScenario<T>,
) : Execution<T>() {

    override fun execute(): T =
        scenario()
            .let {
                it.run()
                it.result()
                    .apply { step.postExecution.setResult(this) }
            }
}
