package com.github.lemfi.kest.cadence.cli

import com.github.lemfi.kest.cadence.builder.ActivityCallExecutionBuilder
import com.github.lemfi.kest.cadence.builder.WorkflowExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.Step

inline fun <reified R> ScenarioBuilder.`given activity call`(retryStep: RetryStep? = null, crossinline h: ActivityCallExecutionBuilder<R>.()->Unit): Step<R> {
    return Step({ ActivityCallExecutionBuilder<R>(R::class.java).apply(h).build() }, retry = retryStep).apply {
        steps.add(this)
    }
}

inline fun <R> ScenarioBuilder.`given workflow`(retryStep: RetryStep? = null, crossinline h: WorkflowExecutionBuilder<R>.()->Unit): Step<R> {
    return Step({WorkflowExecutionBuilder<R>().apply(h).build()}, retry = retryStep).apply {
        steps.add(this)
    }
}