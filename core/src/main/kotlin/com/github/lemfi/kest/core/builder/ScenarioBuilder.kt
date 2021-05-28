package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.*

sealed class ScenarioBuilder {

    var name: ScenarioName? = null
        private set(value) { field = value }
        get() = field ?: throw IllegalAccessException("a scenario should have a name dude!")

    val steps = mutableListOf<Step<*>>()

    fun name(l: ()->String) {
        name = ScenarioName(l())
    }

    abstract fun toScenario(): IScenario
}

class StandaloneScenarioBuilder: ScenarioBuilder() {

    override fun toScenario(): StandaloneScenario {
        return StandaloneScenario(requireNotNull(name) { "a scenario should have a name dude!" } , steps)
    }
}