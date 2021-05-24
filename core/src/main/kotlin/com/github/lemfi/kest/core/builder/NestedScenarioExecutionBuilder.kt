package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.NestedScenario

class NestedScenarioExecutionBuilder<T>(var scenario: () -> NestedScenario<T>) : ExecutionBuilder<T>() {

    override fun build(): Execution<T> {
        return NestedScenarioStepExecution(scenario)
    }
}