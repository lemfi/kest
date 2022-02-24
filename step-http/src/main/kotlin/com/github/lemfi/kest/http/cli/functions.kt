@file:Suppress("FunctionName")

package com.github.lemfi.kest.http.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution
import com.github.lemfi.kest.http.builder.HttpCallExecutionBuilder
import com.github.lemfi.kest.http.model.HttpResponse

inline fun <reified T> ScenarioBuilder.`given http call`(
    name: String? = null,
    retry: RetryStep? = null,
    noinline h: HttpCallExecutionBuilder<T>.() -> Unit
): StepPostExecution<HttpResponse<T>> {
    val executionBuilder = HttpCallExecutionBuilder(T::class.java)

    return StandaloneStep<HttpResponse<T>>(
        scenarioName = scenarioName,
        name = name?.let { StepName(it) } ?: StepName("HTTP call"),
        retry = retry
    ).addToScenario(executionBuilder, h)
}