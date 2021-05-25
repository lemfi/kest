package com.github.lemfi.kest.cadence.cli

import com.github.lemfi.kest.cadence.builder.ActivityCallExecutionBuilder
import com.github.lemfi.kest.cadence.builder.WorkflowExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution

inline fun <reified R> ScenarioBuilder.`given activity call`(
    name: String? = null,
    retryStep: RetryStep? = null,
    crossinline h: ActivityCallExecutionBuilder<R>.() -> Unit
): StepPostExecution<R> {
    return Step(
        name = name?.let { StepName(it) } ?: StepName("cadence activity"),
        scenarioName = this.name!!,
        execution = { ActivityCallExecutionBuilder<R>(R::class.java).apply(h).build() },
        retry = retryStep
    )
        .apply { steps.add(this) }
        .postExecution
}

inline fun <R> ScenarioBuilder.`given workflow`(
    name: String? = null,
    retryStep: RetryStep? = null,
    crossinline h: WorkflowExecutionBuilder<R>.() -> Unit
): StepPostExecution<R> {
    return Step(
        name = name?.let { StepName(it) } ?: StepName("cadence workflow"),
        scenarioName = this.name!!,
        execution = { WorkflowExecutionBuilder<R>().apply(h).build() },
        retry = retryStep
    )
        .apply { steps.add(this) }
        .postExecution
}