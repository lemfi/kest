package com.github.lemfi.kest.core.executor

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.Scenario

class NestedScenarioStepExecution<T>(
        val scenario: ()->Scenario<T>,
): Execution<T>() {

    override fun execute(): T {
        return scenario().run()
    }
}