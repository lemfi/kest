package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.*
import com.github.lemfi.kest.core.model.*
import org.opentest4j.AssertionFailedError

fun scenario(s: ScenarioBuilder.() -> Unit): Scenario {
    return StandaloneScenarioBuilder().apply(s).toScenario()
}

infix fun <I, T, R> IStepPostExecution<I, T, R>.`assert that`(l: AssertionsBuilder.(stepResult: I) -> Unit): IStepPostExecution<I, T, R> {
    addAssertion(l)
    return this
}

fun <T> ScenarioBuilder.step(name: String? = null, retryStep: RetryStep? = null, l: NestedScenarioExecutionBuilder<T>.() -> Unit): StepPostExecution<T> {
    val executionBuilder = NestedScenarioExecutionBuilder<T>(name)

    return NestedScenarioStep<T>(
        name = name?.let { StepName(it) },
        scenarioName = this.name!!,
        retry = retryStep
    )
        .apply { executionBuilder.step = this }
        .addToScenario(this, executionBuilder, l)
}

fun <T, E: ExecutionBuilder<T>> Step<T>.addToScenario(
    scenario: ScenarioBuilder,
    executionBuilder: E,
    executionConfiguration: E.() -> Unit
): StepPostExecution<T> =
    let { step ->
        scenario.steps.add(this)
        step.execution = { executionBuilder.apply(executionConfiguration).toExecution() }
        step.postExecution
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
        step.postExecution.assertions.forEach { assert ->
            assertion.assert(res)
        }
        step.postExecution.setResult(res)

    } catch (e: AssertionFailedError) {
        if (retry > 0) {
            Thread.sleep(delay)
            retryableStepExecution(retry - 1, delay, step, execution)
        } else
            throw e
    } catch (e: Throwable) {
        assertion.fail(e.localizedMessage, e)
    }
}