package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.builder.NestedScenarioExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.model.Step

fun scenario(s: ScenarioBuilder.()->Unit): Scenario {
    return ScenarioBuilder().apply(s).build()
}

infix fun <T> Step<T>.`assert that`(l: AssertionsBuilder.(stepResult: T)->Unit): Step<T> {
    assertions.add(l)
    return this
}

infix fun ScenarioBuilder.steps(l: ScenarioBuilder.()->Unit): Step<Unit> {
    return Step({ NestedScenarioExecutionBuilder { ScenarioBuilder().apply(l).build() }.build() }).apply { steps.add(this) }
}

@Suppress("unchecked_cast")
fun Scenario.run() {

    this.steps.forEach { (it as Step<Any>).run() }
}

private fun Step<Any>.run() {

    with(execution()) {

        val res = execute()

        assertions.forEach {
            AssertionsBuilder().it(res)
        }

        withResult(res)
    }
}