package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.*
import com.github.lemfi.kest.core.model.*
import org.opentest4j.AssertionFailedError

fun scenario(s: ScenarioBuilder.() -> Unit): Scenario {
    return StandaloneScenarioBuilder().apply(s).build()
}

infix fun <I, T, R> IStepPostExecution<I, T, R>.`assert that`(l: AssertionsBuilder.(stepResult: I) -> Unit): IStepPostExecution<I, T, R> {
    addAssertion(l)
    return this
}

fun <T> ScenarioBuilder.step(name: String? = null, retryStep: RetryStep? = null, l: NestedScenarioBuilder<T>.() -> Unit): StepPostExecution<T> {
    return Step(
        name = name?.let { StepName(it) },
        scenarioName = this.name!!,
        execution =  {
            NestedScenarioExecutionBuilder {
                NestedScenarioBuilder<T>()
                    .apply{ name { (this@step.name as ScenarioName).name } }
                    .apply(l).build()
            }.build()
        },
        retry = retryStep
    )
        .apply { steps.add(this) }
        .postExecution
}

@JvmName("anyStep")
inline fun ScenarioBuilder.step(name: String? = null, retryStep: RetryStep? = null, crossinline l: NestedScenarioBuilder<Any>.() -> Unit) {
    Step(
        name = name?.let { StepName(it) },
        scenarioName = this.name!!,
        execution = {
            NestedScenarioExecutionBuilder {
                NestedScenarioBuilder<Any>()
                    .apply{ name { (this@step.name as ScenarioName).name }}
                    .apply { returns {  } }
                    .apply(l).build()
            }.build()
        },
        retry = retryStep
    ).apply { steps.add(this) }
}

@Suppress("unchecked_cast")
fun IScenario.run() {

    return steps.forEach { (it as Step<Any>).run() }
}

private fun Step<Any>.run(): Step<Any> {

    val execution = execution()
    retryableStepExecution(retry?.retries ?: 0, retry?.delay ?: 0L, this, execution)
    return this
}

private fun retryableStepExecution(retry: Int, delay: Long, step: Step<Any>, execution: Execution<Any>) {

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
        } else assertion.fail(e.localizedMessage, e)
    } catch (e: Throwable) {
        assertion.fail(e.localizedMessage, e)
    }
}