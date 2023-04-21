@file:Suppress("FunctionName")

package com.github.lemfi.kest.junit5.runner

import com.github.lemfi.kest.core.builder.StandaloneScenarioBuilder
import com.github.lemfi.kest.core.cli.nestedScenario
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.properties.autoconfigure
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode

internal fun IScenario.toDynamicContainer(
    beforeEach: (() -> Scenario)? = null,
    afterEach: (() -> Scenario)? = null,
): DynamicContainer {
    return DynamicContainer.dynamicContainer(name,

        object : Iterable<DynamicNode> {
            override fun iterator(): Iterator<DynamicNode> {
                return iterator {
                    beforeEach?.also { yieldAll(createBeforeAfterScenario(beforeEach) as Iterator<DynamicNode>) }
                    yieldAll(ScenarioStepsIterator(this@toDynamicContainer) as Iterator<DynamicNode>)
                    afterEach?.also { yieldAll(createBeforeAfterScenario(afterEach) as Iterator<DynamicNode>) }
                }
            }
        }

    )
}

private fun createBeforeAfterScenario(scenarioBuilder: () -> Scenario): ScenarioStepsIterator {
    val scenario = scenarioBuilder()

    return ScenarioStepsIterator(
        scenario("before or after") {
            nestedScenario(scenario.name) {
                this.steps.addAll(scenario.steps)
            }
        }
    )
}

@Deprecated("use playScenarios instead", replaceWith = ReplaceWith("playScenarios(*scenario, beforeEach = beforeEach, beforeAll = beforeAll, afterEach = afterEach, afterAll = afterAll)"))
fun `play scenarios`(
    vararg scenario: Scenario,
    beforeEach: (() -> Scenario)? = null,
    beforeAll: (() -> Scenario)? = null,
    afterEach: (() -> Scenario)? = null,
    afterAll: (() -> Scenario)? = null,
): List<DynamicNode> =
    playScenarios(
        scenario = scenario,
        beforeEach = beforeEach,
        beforeAll = beforeAll,
        afterEach = afterEach,
        afterAll = afterAll
    )


fun playScenarios(
    vararg scenario: Scenario,
    beforeEach: (() -> Scenario)? = null,
    beforeAll: (() -> Scenario)? = null,
    afterEach: (() -> Scenario)? = null,
    afterAll: (() -> Scenario)? = null,
): List<DynamicNode> {

    autoconfigure()

    return mutableListOf<DynamicContainer>().apply {
        beforeAll?.apply {
            add(invoke().toDynamicContainer())
        }
        addAll(scenario.map { it.toDynamicContainer(beforeEach, afterEach) })
        afterAll?.apply {
            add(invoke().toDynamicContainer())
        }
    }
}

@Deprecated("use playScenario instead")
fun `play scenario`(
    name: String = "anonymous scenario",
    unwrap: Boolean = true,
    l: StandaloneScenarioBuilder.() -> Unit,
) = playScenario(name, unwrap, l)


fun playScenario(
    name: String = "anonymous scenario",
    unwrap: Boolean = true,
    l: StandaloneScenarioBuilder.() -> Unit,
) =
    StandaloneScenarioBuilder(name).apply(l).toScenario()
        .let { playScenario(scenario = it, unwrap = unwrap) }

@Deprecated("use playScenario instead", replaceWith = ReplaceWith("playScenario(scenario = scenario, unwrap = unwrap)"))
fun `play scenario`(scenario: Scenario, unwrap: Boolean = true) =
    playScenario(scenario, unwrap)

fun playScenario(scenario: Scenario, unwrap: Boolean = true) = autoconfigure()
    .let { if (unwrap) ScenarioStepsIterator(scenario) else scenario.toDynamicContainer() }
