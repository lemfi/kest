package com.github.lemfi.kest.cadence.cli

import com.github.lemfi.kest.cadence.builder.ActivityCallExecutionBuilder
import com.github.lemfi.kest.cadence.builder.WorkflowExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.Step

inline fun <R> ScenarioBuilder.`given activity call`(crossinline h: ActivityCallExecutionBuilder<R>.()->Unit): Step<R> {
    return Step({ ActivityCallExecutionBuilder<R>().apply(h).build() }).apply {
        steps.add(this)
    }
}

inline fun <R> ScenarioBuilder.`given workflow`(crossinline h: WorkflowExecutionBuilder<R>.()->Unit): Step<R> {
    return Step({WorkflowExecutionBuilder<R>().apply(h).build()}).apply {
        steps.add(this)
    }
}