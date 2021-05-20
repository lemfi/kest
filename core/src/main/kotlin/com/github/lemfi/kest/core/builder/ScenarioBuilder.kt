package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.model.Step

class IScenarioBuilder<T> {

    var name = ""

    val steps = mutableListOf<Step<*>>()

    var result: (()->T)? = null

    fun build(): Scenario<T> {
        return Scenario(name, steps, result)
    }
}

typealias ScenarioBuilder = IScenarioBuilder<*>