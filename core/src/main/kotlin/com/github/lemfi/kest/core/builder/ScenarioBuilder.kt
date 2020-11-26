package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.model.Step

class ScenarioBuilder {

    var name = ""

    val steps = mutableListOf<Step<*>>()

    fun build(): Scenario {
        return Scenario(name, steps)
    }
}