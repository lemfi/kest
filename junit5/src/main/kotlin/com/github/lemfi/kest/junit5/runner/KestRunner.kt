package com.github.lemfi.kest.junit5.runner

import com.github.lemfi.kest.core.builder.StandaloneScenarioBuilder
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.properties.autoconfigure
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest

fun IScenario.toDynamicContainer(beforeEach: (() -> Unit)? = null, afterEach: (() -> Unit)? = null): DynamicContainer {
    return DynamicContainer.dynamicContainer(name.value,

        object: Iterable<DynamicNode> {
            override fun iterator(): Iterator<DynamicNode> {
                return iterator {
                    beforeEach?.also { yield(DynamicTest.dynamicTest("prepare scenario", it)) }
                    yieldAll(ScenarioStepsIterator(this@toDynamicContainer) as Iterator<DynamicNode>)
                    afterEach?.also { yield(DynamicTest.dynamicTest("cleanup scenario", it)) }
                }
            }
        }

    )
}

fun `play scenarios`(
    vararg scenario: Scenario,
    beforeEach: () -> Unit = {},
    afterEach: () -> Unit = {}
): List<DynamicNode> {

    autoconfigure()

    return mutableListOf<DynamicContainer>().apply {
        addAll(scenario.map { it.toDynamicContainer(beforeEach, afterEach) })
    }
}

fun `play scenario`(unwrap: Boolean = true, l: StandaloneScenarioBuilder.()->Unit) =
    StandaloneScenarioBuilder().apply(l).toScenario()
        .let { `play scenario`(it, unwrap) }

fun `play scenario`(scenario: Scenario, unwrap: Boolean = true) = autoconfigure().let {
    if (unwrap) ScenarioStepsIterator(scenario) else scenario.toDynamicContainer()
}