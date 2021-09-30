package com.github.lemfi.kest.core.executor

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.NestedScenario
import com.github.lemfi.kest.core.model.Step

class NestedScenarioStepExecution<T>(
    val step: Step<T>,
    val scenario: () -> NestedScenario<T>,
) : Execution<T>() {

    override fun execute(): T =

        scenario()
            .run {
                run()
                resolve()
            }
}
