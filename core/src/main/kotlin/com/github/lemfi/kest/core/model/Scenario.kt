package com.github.lemfi.kest.core.model

typealias Scenario = StandaloneScenario

@JvmInline
value class ScenarioName(val value: String)

sealed class IScenario {
    abstract val name: ScenarioName
    abstract val steps: MutableList<Step<*>>
}

class StandaloneScenario(
    override val name: ScenarioName,
    override val steps: MutableList<Step<*>>,
): IScenario()

class NestedScenario<T>(
    override val name: ScenarioName,
    val parentStep: Step<T>,
    override val steps: MutableList<Step<*>>,
    val result: () -> T
): IScenario()