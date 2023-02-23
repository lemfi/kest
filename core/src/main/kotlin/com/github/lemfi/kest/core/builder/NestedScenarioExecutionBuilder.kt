package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.NestedScenario
import com.github.lemfi.kest.core.model.Step

class NestedScenarioExecutionBuilder<RESULT>(name: String?) : ScenarioBuilder(name ?: "anonymous nested scenario"),
    ExecutionBuilder<RESULT> {

    lateinit var step: Step<RESULT>

    override fun toExecution(): Execution<RESULT> {
        return NestedScenarioStepExecution(step) { toScenario() }
    }

    private var result: () -> RESULT = { throw IllegalArgumentException("A nested scenario must have a result!") }

    fun returns(l: () -> RESULT) {
        result = l
    }

    override fun toScenario(): NestedScenario<RESULT> {
        return NestedScenario(name, step, steps, result)
    }
}