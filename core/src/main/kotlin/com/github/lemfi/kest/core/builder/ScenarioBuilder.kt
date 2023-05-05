package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.DefaultStepName
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.IStepName
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.core.model.NestedScenarioStepResult
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StandaloneScenario
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StandaloneStepResult
import com.github.lemfi.kest.core.model.Step

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
    fun <RESULT> createStep(
        name: IStepName = DefaultStepName("generic step"),
        retry: RetryStep? = null,
        builder: () -> ExecutionBuilder<RESULT>
    ) =
        StandaloneStep<RESULT>(name = name, retry = retry, scenarioName = scenarioName)
            .also { it.execution = { builder().toExecution() } }
            .apply { steps.add(this) }
            .future as StandaloneStepResult<RESULT>

    fun <RESULT> createNestedScenarioStep(
        name: IStepName = DefaultStepName("generic step"),
        builder: () -> NestedScenarioExecutionBuilder<RESULT>
    ) =
        NestedScenarioStep<RESULT>(name = name, scenarioName = scenarioName)
            .also { it.execution = { builder().apply { step = it }.toExecution() } }
            .apply { steps.add(this) }
            .future as NestedScenarioStepResult<RESULT>

}

class StandaloneScenarioBuilder(name: String = "anonymous scenario") : ScenarioBuilder(name) {

    override fun toScenario(): StandaloneScenario {
        return StandaloneScenario(name, steps)
    }
}