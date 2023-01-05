package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.DefaultStepName
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.IStepName
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.core.model.NestedScenarioStepPostExecution
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StandaloneScenario
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepPostExecution

sealed class ScenarioBuilder(protected var name: String = "anonymous scenario") {

    val scenarioName = name

    val steps = mutableListOf<Step<*>>()

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(
        message = "scenario name should be set on scenario creation function: scenario(name = \"the name\") { }",
        level = DeprecationLevel.WARNING
    )
    fun name(l: () -> String) {
        name = l()
    }

    abstract fun toScenario(): IScenario

    @Suppress("UNCHECKED_CAST")
    fun <T> createStep(
        name: IStepName = DefaultStepName("generic step"),
        retry: RetryStep? = null,
        builder: () -> ExecutionBuilder<T>
    ) =
        StandaloneStep<T>(name = name, retry = retry, scenarioName = scenarioName)
            .also { it.execution = { builder().toExecution() } }
            .apply { steps.add(this) }
            .postExecution as StepPostExecution<T>

    fun <T> createNestedScenarioStep(
        name: IStepName = DefaultStepName("generic step"),
        builder: () -> NestedScenarioExecutionBuilder<T>
    ) =
        NestedScenarioStep<T>(name = name, scenarioName = scenarioName)
            .also { it.execution = { builder().apply { step = it }.toExecution() } }
            .apply { steps.add(this) }
            .postExecution as NestedScenarioStepPostExecution<T, T>

}

class StandaloneScenarioBuilder(name: String = "anonymous scenario") : ScenarioBuilder(name) {

    override fun toScenario(): StandaloneScenario {
        return StandaloneScenario(name, steps)
    }
}