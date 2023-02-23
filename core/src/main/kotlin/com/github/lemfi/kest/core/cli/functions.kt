@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.builder.NestedScenarioExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.builder.StandaloneScenarioBuilder
import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.logger.getOrDefault
import com.github.lemfi.kest.core.logger.threadLocalLogger
import com.github.lemfi.kest.core.model.DefaultStepName
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.IStepName
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.model.StandaloneStepPostExecution
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepResultFailure
import org.opentest4j.AssertionFailedError
import org.slf4j.Logger

fun scenario(name: String = "anonymous scenario", s: ScenarioBuilder.() -> Unit): Scenario {
    return StandaloneScenarioBuilder(name).apply(s).toScenario()
}

infix fun <I, T, R> StandaloneStepPostExecution<I, T, R>.`assert that`(l: AssertionsBuilder.(stepResult: I) -> Unit): StandaloneStepPostExecution<I, T, R> {
    addAssertion(l)
    return this
}

@Suppress("BlockingMethodInNonBlockingContext")
fun ScenarioBuilder.wait(time: Long, name: String? = null) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("wait $time ms"),
        retry = null,
    ) {
        object : ExecutionBuilder<Unit> {
            override fun toExecution(): Execution<Unit> = object : Execution<Unit>() {
                override fun execute() {
                    Thread.sleep(time)
                }
            }
        }

    }

fun <RESULT> ScenarioBuilder.step(
    name: String? = null,
    retry: RetryStep? = null,
    l: (Logger) -> RESULT
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("generic step"),
        retry = retry,
    ) {
        object : ExecutionBuilder<RESULT> {
            override fun toExecution(): Execution<RESULT> = object : Execution<RESULT>() {
                override fun execute(): RESULT = l(LoggerFactory.getLogger("KEST"))
            }
        }
    }

fun <RESULT> ScenarioBuilder.nestedScenario(
    name: String? = null,
    l: NestedScenarioExecutionBuilder<RESULT>.() -> Unit
) =

    createNestedScenarioStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("nested scenario step"),
    ) {
        NestedScenarioExecutionBuilder<RESULT>(name).apply(l)
    }


@JvmName("voidNestedScenario")
fun ScenarioBuilder.nestedScenario(
    name: String? = null,
    l: NestedScenarioExecutionBuilder<Unit>.() -> Unit
) =

    createNestedScenarioStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("nested scenario step"),
    ) {
        NestedScenarioExecutionBuilder<Unit>(name)
            .apply {
                returns {
                    steps.onEach { if (it.postExecution.isFailed()) it.postExecution() }
                }
            }
            .apply(l)
    }

@Suppress("unchecked_cast")
fun IScenario.run() {

    steps.forEach { (it as Step<Any>).run() }
}

fun <RESULT> Step<RESULT>.run(): Step<RESULT> {

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
                (postExecution as StandaloneStepPostExecution<RESULT, *, *>).assertions.forEach { assert ->
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
        } finally {
            if (tries == 0) {
                execution.onExecutionEnded()
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
    stepName: IStepName?
): String {

    val messages = message?.lines()?.flatMap { it.chunked(200) } ?: emptyList()
    val scenario = "Scenario: $scenarioName"
    val step = if (stepName != null) "    Step: ${stepName.value}" else ""

    return """
        
        
        $scenario
        $step
        
        ${messages.joinToString("\n")}
        
    """.trimIndent()
}