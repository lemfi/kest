@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.builder.NestedScenarioExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.builder.StandaloneScenarioBuilder
import com.github.lemfi.kest.core.logger.getOrDefault
import com.github.lemfi.kest.core.logger.threadLocalLogger
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.core.model.NestedScenarioStepPostExecution
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StandaloneStepPostExecution
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution
import com.github.lemfi.kest.core.model.StepResultFailure
import org.opentest4j.AssertionFailedError

fun scenario(name: String = "anonymous scenario", s: ScenarioBuilder.() -> Unit): Scenario {
    return StandaloneScenarioBuilder(name).apply(s).toScenario()
}

infix fun <I, T, R> StandaloneStepPostExecution<I, T, R>.`assert that`(l: AssertionsBuilder.(stepResult: I) -> Unit): StandaloneStepPostExecution<I, T, R> {
    addAssertion(l)
    return this
}

fun ScenarioBuilder.wait(time: Long, name: String? = null): StepPostExecution<Unit> {
    val executionBuilder = object : ExecutionBuilder<Unit> {
        override fun toExecution(): Execution<Unit> = object : Execution<Unit>() {
            override fun execute() {
                Thread.sleep(time)
            }
        }
    }

    return StandaloneStep<Unit>(
        scenarioName = scenarioName,
        name = name?.let { StepName(it) } ?: StepName("wait $time ms"),
        retry = null
    )
        .addToScenario(executionBuilder) {}
}

fun <T> ScenarioBuilder.step(name: String? = null, retry: RetryStep? = null, l: () -> T): StepPostExecution<T> {
    val executionBuilder = object : ExecutionBuilder<T> {
        override fun toExecution(): Execution<T> = object : Execution<T>() {
            override fun execute(): T = l()
        }
    }

    return StandaloneStep<T>(
        scenarioName = scenarioName,
        name = name?.let { StepName(it) } ?: StepName("generic step"),
        retry = retry
    )
        .addToScenario(executionBuilder) {}
}

fun <T> ScenarioBuilder.nestedScenario(
    name: String? = null,
    l: NestedScenarioExecutionBuilder<T>.() -> Unit
): NestedScenarioStepPostExecution<T, T> {
    val executionBuilder = NestedScenarioExecutionBuilder<T>(name)

    return NestedScenarioStep<T>(
        name = name?.let { StepName(it) },
        scenarioName = scenarioName,
    )
        .apply { executionBuilder.step = this }
        .addToScenario(executionBuilder, l)
}

@JvmName("voidNestedScenario")
fun ScenarioBuilder.nestedScenario(
    name: String? = null,
    l: NestedScenarioExecutionBuilder<Unit>.() -> Unit
): NestedScenarioStepPostExecution<Unit, Unit> {
    val executionBuilder = NestedScenarioExecutionBuilder<Unit>(name)
        .apply {
            returns {
                steps.onEach { if (it.postExecution.isFailed()) it.postExecution() }
            }
        }

    return NestedScenarioStep<Unit>(
        name = name?.let { StepName(it) },
        scenarioName = scenarioName,
    )
        .apply { executionBuilder.step = this }
        .addToScenario(executionBuilder, l)
}

@Suppress("unchecked_cast")
fun IScenario.run() {

    steps.forEach { (it as Step<Any>).run() }
}

fun <T> Step<T>.run(): Step<T> {

    threadLocalLogger.getOrDefault().reset()

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
                    runCatching {
                        assertion.assert(res)
                    }.onFailure {
                        assertion.fail(it)
                    }
                }
            }
            tries = 0
            execution.onAssertionSuccess()
            postExecution.setResult(res)
        } catch (e: Throwable) {
            execution.onAssertionFailedError()
            if (e is StepResultFailure) throw e
            tries--
            if (tries > 0) {
                Thread.sleep(delay)
            } else if (e is AssertionFailedError) {
                postExecution.setFailed(e)
                throw e
            } else {
                postExecution.setFailed(e)
                assertion.fail(e)
            }
        }
    }

    return this
}

private fun AssertionsBuilder.fail(cause: Throwable) {

    if (cause is AssertionFailedError && cause.cause == null) {

        throw AssertionFailedError(failureMessage(cause.message, stepName), cause.expected, cause.actual)
            .apply { stackTrace = cause.stackTrace }
    }
    throw AssertionFailedError(failureMessage(cause.message, stepName))
        .apply { stackTrace = cause.stackTrace }
}

private fun AssertionsBuilder.failureMessage(
    message: String?,
    stepName: StepName?
): String {

    val messages = message?.lines()?.flatMap { it.chunked(80) } ?: listOf("null")
    val scenario = "Scenario: $scenarioName".lines().flatMap { it.chunked(80) }
    val step =
        (if (stepName != null) "Step: ${stepName.value}".chunked(80) else emptyList()).let {
            if (scenario.size > 1) listOf("", *it.toTypedArray()) else it
        }
    val max =
        listOf(*scenario.toTypedArray(), *step.toTypedArray(), *messages.toTypedArray()).maxByOrNull { it.length }!!

    return """
        
        +${(0..max.length + 1).joinToString("") { "-" }}+
        ${scenario.joinToString("\n        ") { "| ${it.padEnd(max.length, ' ')} |" }}
        ${step.joinToString("\n        ") { "| ${it.padEnd(max.length, ' ')} |" }}
        |${(0..max.length + 1).joinToString("") { " " }}|
        ${messages.joinToString("\n        ") { "| ${it.padEnd(max.length, ' ')} |" }}
        +${(0..max.length + 1).joinToString("") { "-" }}+
        
    """.trimIndent()
}