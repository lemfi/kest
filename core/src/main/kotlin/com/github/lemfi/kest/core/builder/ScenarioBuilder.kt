package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.core.model.NestedScenarioStepPostExecution
import com.github.lemfi.kest.core.model.ScenarioName
import com.github.lemfi.kest.core.model.StandaloneScenario
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StandaloneStepPostExecution
import com.github.lemfi.kest.core.model.Step

sealed class ScenarioBuilder {

    var name: ScenarioName = ScenarioName("anonymous scenario")
        private set

    val steps = mutableListOf<Step<*>>()

    fun name(l: () -> String) {
        name = ScenarioName(l())
    }

    abstract fun toScenario(): IScenario

    fun <T, E : ExecutionBuilder<T>> StandaloneStep<T>.addToScenario(
        executionBuilder: E,
        executionConfiguration: E.() -> Unit
    ): StandaloneStepPostExecution<T, T, T> =
        let { step ->
            steps.add(this)
            step.execution = { executionBuilder.apply(executionConfiguration).toExecution() }
            @Suppress("unchecked_cast")
            step.postExecution as StandaloneStepPostExecution<T, T, T>
        }

    fun <T, E : ExecutionBuilder<T>> NestedScenarioStep<T>.addToScenario(
        executionBuilder: E,
        executionConfiguration: E.() -> Unit
    ): NestedScenarioStepPostExecution<T, T> =
        let { step ->
            steps.add(this)
            step.execution = { executionBuilder.apply(executionConfiguration).toExecution() }
            step.postExecution as NestedScenarioStepPostExecution<T, T>
        }
}

class StandaloneScenarioBuilder : ScenarioBuilder() {

    override fun toScenario(): StandaloneScenario {
        return StandaloneScenario(name, steps)
    }
}