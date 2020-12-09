package com.github.lemfi.kest.core.executor

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.Scenario

class NestedScenarioStepExecution(
        val scenario: ()->Scenario,
        override val withResult: Unit.()->Unit = {},
): Execution<Unit>() {

    override fun execute() {
        scenario().run()
    }
}