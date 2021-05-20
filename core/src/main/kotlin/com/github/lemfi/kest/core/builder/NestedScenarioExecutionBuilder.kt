package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.Scenario

class NestedScenarioExecutionBuilder<T>(var scenario: ()->Scenario<T>): ExecutionBuilder<T>() {

    override fun build(): Execution<T> {
        return NestedScenarioStepExecution(scenario)
    }
}