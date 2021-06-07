package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.*
import com.github.lemfi.kest.core.model.*
import org.opentest4j.AssertionFailedError

fun scenario(s: ScenarioBuilder.() -> Unit): Scenario {
    return StandaloneScenarioBuilder().apply(s).toScenario()
}

infix fun <I, T, R> StandaloneStepPostExecution<I, T, R>.`assert that`(l: AssertionsBuilder.(stepResult: I) -> Unit): StandaloneStepPostExecution<I, T, R> {
    addAssertion(l)
    return this
}

fun <T> ScenarioBuilder.step(name: String? = null, retryStep: RetryStep? = null, l: NestedScenarioExecutionBuilder<T>.() -> Unit): NestedScenarioStepPostExecution<T, T> {
    val executionBuilder = NestedScenarioExecutionBuilder<T>(name)

    return NestedScenarioStep<T>(
        name = name?.let { StepName(it) },
        scenarioName = this.name!!,
        retry = retryStep
    )
        .apply { executionBuilder.step = this }
        .addToScenario(this, executionBuilder, l)
}

fun <T, E: ExecutionBuilder<T>> StandaloneStep<T>.addToScenario(
    scenario: ScenarioBuilder,
    executionBuilder: E,
    executionConfiguration: E.() -> Unit
): StandaloneStepPostExecution<T, T, T> =
    let { step ->
        scenario.steps.add(this)
        step.execution = { executionBuilder.apply(executionConfiguration).toExecution() }
        @Suppress("unchecked_cast")
        step.postExecution as StandaloneStepPostExecution<T, T, T>
    }

fun <T, E: ExecutionBuilder<T>> NestedScenarioStep<T>.addToScenario(
    scenario: ScenarioBuilder,
    executionBuilder: E,
    executionConfiguration: E.() -> Unit
): NestedScenarioStepPostExecution<T, T> =
    let { step ->
        scenario.steps.add(this)
        step.execution = { executionBuilder.apply(executionConfiguration).toExecution() }
        step.postExecution as NestedScenarioStepPostExecution<T, T>
    }

@JvmName("noResultStep")
fun ScenarioBuilder.step(name: String? = null, retryStep: RetryStep? = null, l: NestedScenarioExecutionBuilder<Any>.() -> Unit) {
    val executionBuilder = NestedScenarioExecutionBuilder<Any>(name).apply { returns {} }

    NestedScenarioStep<Any>(
        name = name?.let { StepName(it) },
        scenarioName = this.name!!,
        retry = retryStep
    )
        .apply { executionBuilder.step = this }
        .addToScenario(this, executionBuilder, l)

}

@Suppress("unchecked_cast")
fun IScenario.run() {

    return steps.forEach { (it as Step<Any>).run() }
}

fun <T> Step<T>.run(): Step<T> {

    val execution = execution()
    retryableStepExecution(retry?.retries ?: 0, retry?.delay ?: 0L, this, execution)
    return this
}

private fun <T> retryableStepExecution(retry: Int, delay: Long, step: Step<T>, execution: Execution<T>) {

    val assertion = AssertionsBuilder(step.scenarioName, step.name, execution.description)

    try {
        val res = execution.execute()
        if (step.postExecution is StandaloneStepPostExecution<*, *, *>) {
            @Suppress("unchecked_cast")
            (step.postExecution as StandaloneStepPostExecution<T, *, *>).assertions.forEach { assert ->
                assertion.assert(res)
            }
        }
        step.postExecution.setResult(res)

    } catch (e: Throwable) {
        if (retry > 0) {
            Thread.sleep(delay)
            retryableStepExecution(retry - 1, delay, step, execution)
        } else if (e is AssertionFailedError) throw e
        else assertion.fail(e.message ?: "null", e)
    }
}