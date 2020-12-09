package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.Scenario

class NestedScenarioExecutionBuilder(var scenario: ()->Scenario): ExecutionBuilder<Unit>() {

    private val withResult: Unit.()->Unit = {}

    override fun build(): Execution<Unit> {
        return NestedScenarioStepExecution(scenario, withResult)
    }
}