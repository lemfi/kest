package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.builder.IScenarioBuilder
import com.github.lemfi.kest.core.builder.NestedScenarioExecutionBuilder
import com.github.lemfi.kest.core.model.*
import org.opentest4j.AssertionFailedError
import java.lang.IllegalArgumentException

fun scenario(s: IScenarioBuilder<Any>.()->Unit): Scenario<Any> {
    return IScenarioBuilder<Any>().apply(s).build()
}

infix fun <I, T, R> IStepPostExecution<I, T, R>.`assert that`(l: AssertionsBuilder.(stepResult: I)->Unit): IStepPostExecution<I, T, R> {
    assertions.add(l)
    return this
}

inline fun <reified T> IScenarioBuilder<*>.steps(retryStep: RetryStep? = null, crossinline l: IScenarioBuilder<T>.()->Unit): StepPostExecution<T> {
    return Step({ NestedScenarioExecutionBuilder { IScenarioBuilder<T>().apply(l).build() }.build() }, retry = retryStep).apply { steps.add(this) }.postExecution
}

@JvmName("anySteps")
inline fun IScenarioBuilder<*>.steps(retryStep: RetryStep? = null, crossinline l: IScenarioBuilder<Any>.()->Unit): StepPostExecution<*> {
    return Step({ NestedScenarioExecutionBuilder { IScenarioBuilder<Any>().apply(l).build() }.build() }, retry = retryStep)
        .apply { steps.add(this) }
        .postExecution
}

@Suppress("unchecked_cast")
fun <T> Scenario<T>.run(): T {

    return this.steps
        .map { (it as Step<Any>).run() }
        .lastOrNull()
        ?.let {
            this.result?.run { this() } ?: it.postExecution.result() as T
        } ?: throw IllegalArgumentException("You are running a scenario without any step")
}

private fun Step<Any>.run(): Step<Any> {

    val execution = execution()
    retryableStepExecution(retry?.retries ?: 0, retry?.delay ?: 0L, this, execution)
    return this
}

private fun retryableStepExecution(retry: Int, delay: Long, step: Step<Any>, execution: Execution<Any>) {

    try {
        val res = execution.execute()
        step.postExecution.assertions.forEach {
            AssertionsBuilder().it(res)
        }
        step.postExecution.setResult(res)

    } catch (e: AssertionFailedError) {
        if (retry > 0) {
            Thread.sleep(delay)
            retryableStepExecution(retry - 1, delay, step, execution)
        } else throw e
    }
}