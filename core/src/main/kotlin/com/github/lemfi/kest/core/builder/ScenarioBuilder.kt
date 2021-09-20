package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.*

sealed class ScenarioBuilder {

    var name: ScenarioName? = null
        private set(value) {
            field = value
        }
        get() = field ?: throw IllegalAccessException("a scenario should have a name dude!")

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
        return StandaloneScenario(requireNotNull(name) { "a scenario should have a name dude!" }, steps)
    }
}