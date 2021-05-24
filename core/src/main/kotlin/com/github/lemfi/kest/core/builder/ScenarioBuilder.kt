package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.*

sealed class ScenarioBuilder {

    var name: ScenarioName? = null
        private set(value) { field = value }

    val steps = mutableListOf<Step<*>>()

    fun name(l: ()->String) {
        name = ScenarioName(l())
    }

    abstract fun build(): IScenario
}

class StandaloneScenarioBuilder: ScenarioBuilder() {

    override fun build(): StandaloneScenario {
        return StandaloneScenario(requireNotNull(name) { "a scenario should have a name dude!" } , steps)
    }
}

class NestedScenarioBuilder<T>: ScenarioBuilder() {

    private var result: () -> T = { throw IllegalArgumentException("A nested scenario must have a result!") }

    fun returns(l: () -> T) {
        result = l
    }

    override fun build(): NestedScenario<T> {
        return NestedScenario(name!!, steps, result)
    }
}