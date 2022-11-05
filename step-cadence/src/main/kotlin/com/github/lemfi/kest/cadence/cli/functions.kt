@file:Suppress("FunctionName")

package com.github.lemfi.kest.cadence.cli

import com.github.lemfi.kest.cadence.builder.ActivityCallExecutionBuilder
import com.github.lemfi.kest.cadence.builder.WorkflowExecutionBuilder
import com.github.lemfi.kest.cadence.model.cadenceProperty
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.DefaultStepName
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution
import com.google.gson.reflect.TypeToken
import com.uber.cadence.RegisterDomainRequest
import com.uber.cadence.serviceclient.ClientOptions
import com.uber.cadence.serviceclient.WorkflowServiceTChannel

private val logger = LoggerFactory.getLogger("CADENCE-Kest")

inline fun <reified R> ScenarioBuilder.`given activity call`(
    name: String? = null,
    retryStep: RetryStep? = null,
    noinline h: ActivityCallExecutionBuilder<R>.() -> Unit
): StepPostExecution<R> {

    val executionBuilder = ActivityCallExecutionBuilder<R>(object : TypeToken<R>() {}.type)

    return StandaloneStep<R>(
        name = name?.let { StepName(it) } ?: DefaultStepName("cadence activity"),
        scenarioName = scenarioName,
        retry = retryStep
    ).addToScenario(executionBuilder, h)
}

fun <R> ScenarioBuilder.`given workflow`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: WorkflowExecutionBuilder<R>.() -> Unit
): StepPostExecution<R> {
    val executionBuilder = WorkflowExecutionBuilder<R>()
    return StandaloneStep<R>(
        name = name?.let { StepName(it) } ?: DefaultStepName("cadence workflow"),
        scenarioName = scenarioName,
        retry = retryStep
    ).addToScenario(executionBuilder, h)

}

fun `create domain`(
    name: String,
    cadenceHost: String = cadenceProperty { host },
    cadencePort: Int = cadenceProperty { port }
) =

    WorkflowServiceTChannel(
        ClientOptions.newBuilder()
            .setHost(cadenceHost)
            .setPort(cadencePort)
            .build()
    ).also { cadenceService ->

        try {
            logger.info("Create Cadence domain: $name")
            cadenceService.RegisterDomain(
                RegisterDomainRequest().apply {
                    this.name = name
                    setWorkflowExecutionRetentionPeriodInDays(1)
                }
            )
        } catch (e: Throwable) {
            logger.warn("Domain creation: ${e.message}")
        }
    }