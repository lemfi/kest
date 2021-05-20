package com.github.lemfi.kest.cadence.cli

import com.github.lemfi.kest.cadence.builder.ActivityCallExecutionBuilder
import com.github.lemfi.kest.cadence.builder.WorkflowExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepPostExecution

inline fun <reified R> ScenarioBuilder.`given activity call`(retryStep: RetryStep? = null, crossinline h: ActivityCallExecutionBuilder<R>.()->Unit): StepPostExecution<R> {
    return Step({ ActivityCallExecutionBuilder<R>(R::class.java).apply(h).build() }, retry = retryStep).apply {
        steps.add(this)
    }.postExecution
}

inline fun <R> ScenarioBuilder.`given workflow`(retryStep: RetryStep? = null, crossinline h: WorkflowExecutionBuilder<R>.()->Unit): StepPostExecution<R> {
    return Step({WorkflowExecutionBuilder<R>().apply(h).build()}, retry = retryStep).apply {
        steps.add(this)
    }.postExecution
}