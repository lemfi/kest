package com.github.lemfi.kest.executor.http.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution
import com.github.lemfi.kest.executor.http.builder.HttpCallExecutionBuilder
import com.github.lemfi.kest.executor.http.model.HttpResponse

inline fun <reified T> ScenarioBuilder.`given http call`(
    name: String? = null,
    retry: RetryStep? = null,
    crossinline h: HttpCallExecutionBuilder<T>.() -> Unit
): StepPostExecution<HttpResponse<T>> {
    return Step(
        scenarioName = this.name!!,
        name = name?.let { StepName(it) } ?: StepName("HTTP call"),
        execution = { HttpCallExecutionBuilder(T::class.java).apply(h).build() },
        retry = retry
    )
        .apply { steps.add(this) }
        .postExecution
}