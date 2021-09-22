package com.github.lemfi.kest.junit5.runner

import com.github.lemfi.kest.core.builder.StandaloneScenarioBuilder
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.properties.autoconfigure
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode

internal fun IScenario.toDynamicContainer(
    beforeEach: (() -> Scenario)? = null,
    afterEach: (() -> Scenario)? = null
): DynamicContainer {
    return DynamicContainer.dynamicContainer(name.value,

        object : Iterable<DynamicNode> {
            override fun iterator(): Iterator<DynamicNode> {
                return iterator {
                    beforeEach?.also { yieldAll(ScenarioStepsIterator(beforeEach()) as Iterator<DynamicNode>) }
                    yieldAll(ScenarioStepsIterator(this@toDynamicContainer) as Iterator<DynamicNode>)
                    afterEach?.also { yieldAll(ScenarioStepsIterator(afterEach()) as Iterator<DynamicNode>) }
                }
            }
        }

    )
}

fun `play scenarios`(
    vararg scenario: Scenario,
    beforeEach: (() -> Scenario)? = null,
    afterEach: (() -> Scenario)? = null
): List<DynamicNode> {

    autoconfigure()

    return mutableListOf<DynamicContainer>().apply {
        addAll(scenario.map { it.toDynamicContainer(beforeEach, afterEach) })
    }
}

fun `play scenario`(unwrap: Boolean = true, l: StandaloneScenarioBuilder.() -> Unit) =
    StandaloneScenarioBuilder().apply(l).toScenario()
        .let { `play scenario`(it, unwrap) }

fun `play scenario`(scenario: Scenario, unwrap: Boolean = true) = autoconfigure().let {
    if (unwrap) ScenarioStepsIterator(scenario) else scenario.toDynamicContainer()
}