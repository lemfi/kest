package com.github.lemfi.kest.cadence.cli

import com.github.lemfi.kest.cadence.builder.ActivityCallExecutionBuilder
import com.github.lemfi.kest.cadence.builder.WorkflowExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.cli.addToScenario
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution

inline fun <reified R> ScenarioBuilder.`given activity call`(
    name: String? = null,
    retryStep: RetryStep? = null,
    noinline h: ActivityCallExecutionBuilder<R>.() -> Unit
): StepPostExecution<R> {
    val executionBuilder = ActivityCallExecutionBuilder(R::class.java)

    return StandaloneStep<R>(
        name = name?.let { StepName(it) } ?: StepName("cadence activity"),
        scenarioName = this.name!!,
        retry = retryStep
    ).addToScenario(this, executionBuilder, h)
}

fun <R> ScenarioBuilder.`given workflow`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: WorkflowExecutionBuilder<R>.() -> Unit
): StepPostExecution<R> {
    val executionBuilder = WorkflowExecutionBuilder<R>()
    return StandaloneStep<R>(
        name = name?.let { StepName(it) } ?: StepName("cadence workflow"),
        scenarioName = this.name!!,
        retry = retryStep
    ).addToScenario(this, executionBuilder, h)

}