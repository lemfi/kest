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

fun <T> ScenarioBuilder.step(name: String? = null, retry: RetryStep? = null, l: () -> T): StepPostExecution<T> {
    val executionBuilder = object : ExecutionBuilder<T> {
        override fun toExecution(): Execution<T> = object : Execution<T>() {
            override fun execute(): T = l()
        }
    }

    return StandaloneStep<T>(
        scenarioName = this.name!!,
        name = name?.let { StepName(it) } ?: StepName("generic step"),
        retry = retry
    )
        .addToScenario(executionBuilder) {}
}

fun <T> ScenarioBuilder.nestedScenario(
    name: String? = null,
    retryStep: RetryStep? = null,
    l: NestedScenarioExecutionBuilder<T>.() -> Unit
): NestedScenarioStepPostExecution<T, T> {
    val executionBuilder = NestedScenarioExecutionBuilder<T>(name)

    return NestedScenarioStep<T>(
        name = name?.let { StepName(it) },
        scenarioName = this.name!!,
        retry = retryStep
    )
        .apply { executionBuilder.step = this }
        .addToScenario(executionBuilder, l)
}

@JvmName("noResultStep")
fun ScenarioBuilder.nestedScenario(
    name: String? = null,
    retryStep: RetryStep? = null,
    l: NestedScenarioExecutionBuilder<Any>.() -> Unit
) {
    val executionBuilder = NestedScenarioExecutionBuilder<Any>(name).apply { returns {} }

    NestedScenarioStep<Any>(
        name = name?.let { StepName(it) },
        scenarioName = this.name!!,
        retry = retryStep
    )
        .apply { executionBuilder.step = this }
        .addToScenario(executionBuilder, l)

}

@Suppress("unchecked_cast")
fun IScenario.run() {

    return steps.forEach { (it as Step<Any>).run() }
}

fun <T> Step<T>.run(): Step<T> {

    val execution = try {
        execution()
    } catch (e: Throwable) {
        postExecution.setFailed(e)
        throw e
    }

    val assertion = AssertionsBuilder(scenarioName, name)

    var tries = retry?.retries ?: 1
    val delay = retry?.delay ?: 0L

    while (tries > 0) {

        try {
            val res = execution.execute()
            if (postExecution is StandaloneStepPostExecution<*, *, *>) {
                @Suppress("unchecked_cast")
                (postExecution as StandaloneStepPostExecution<T, *, *>).assertions.forEach { assert ->
                    assertion.assert(res)
                }
            }
            tries = 0
            postExecution.setResult(res)
        } catch (e: Throwable) {
            tries --
            if (tries > 0) {
                Thread.sleep(delay)
            } else if (e is AssertionFailedError) {
                postExecution.setFailed(e)
                throw e
            } else {
                postExecution.setFailed(e)
                assertion.fail(e.message ?: "null", e)
            }
        }
    }

    return this
}