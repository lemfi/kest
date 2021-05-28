package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.model.*

class NestedScenarioExecutionBuilder<T>(name: String?): ScenarioBuilder(), ExecutionBuilder<T> {

    init { name?.also { name { it } } }

    lateinit var step: Step<T>

    override fun toExecution(): Execution<T> {
        return NestedScenarioStepExecution(step) { toScenario() }
    }

    private var result: () -> T = { throw IllegalArgumentException("A nested scenario must have a result!") }

    fun returns(l: () -> T) {
        result = l
    }

    override fun toScenario(): NestedScenario<T> {
        return NestedScenario(name!!, step, steps, result)
    }
}