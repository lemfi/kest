package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.builder.NestedScenarioExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.model.Step
import org.opentest4j.AssertionFailedError

fun scenario(s: ScenarioBuilder.()->Unit): Scenario {
    return ScenarioBuilder().apply(s).build()
}

infix fun <T> Step<T>.`assert that`(l: AssertionsBuilder.(stepResult: T)->Unit): Step<T> {
    assertions.add(l)
    return this
}

fun ScenarioBuilder.steps(retryStep: RetryStep? = null, l: ScenarioBuilder.()->Unit): Step<Unit> {
    return Step({ NestedScenarioExecutionBuilder { ScenarioBuilder().apply(l).build() }.build() }, retry = retryStep).apply { steps.add(this) }
}

@Suppress("unchecked_cast")
fun Scenario.run() {

    this.steps.forEach { (it as Step<Any>).run() }
}

private fun Step<Any>.run() {

    val execution = execution()
    retryableStepExecution(retry?.retries ?: 0, retry?.delay ?: 0L, this, execution)
}

private fun retryableStepExecution(retry: Int, delay: Long, step: Step<Any>, execution: Execution<Any>) {

    try {
        val res = execution.execute()
        step.assertions.forEach {
            AssertionsBuilder().it(res)
        }
        execution.withResult(res)
    } catch (e: AssertionFailedError) {
        if (retry > 0) {
            Thread.sleep(delay)
            retryableStepExecution(retry - 1, delay, step, execution)
        } else throw e
    }
}