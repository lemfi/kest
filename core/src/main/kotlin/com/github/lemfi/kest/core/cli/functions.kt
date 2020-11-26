package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.model.Step

fun scenario(s: ScenarioBuilder.()->Unit): Scenario {
    return ScenarioBuilder().apply(s).build()
}

infix fun <T> Step<T>.`assert that`(l: AssertionsBuilder.(stepResult: T)->Unit): Step<T> {
    assertions = l
    return this
}

@Suppress("unchecked_cast")
fun Scenario.run() {

    this.steps.forEach { (it as Step<Any>).run() }
}

private fun Step<Any>.run() {

    val res = execution.execute()

    AssertionsBuilder().assertions(res)

    execution.withResult(res)
}