@file:Suppress("FunctionName")

package com.github.lemfi.kest.http.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.DefaultStepName
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.http.builder.HttpCallExecutionBuilder

@Suppress("unused")
@Deprecated("use givenHttpCall instead")
inline fun <reified T : Any> ScenarioBuilder.`given http call`(
    name: String? = null,
    retry: RetryStep? = null,
    noinline h: HttpCallExecutionBuilder<T>.() -> Unit
) = givenHttpCall(name, retry, h)

inline fun <reified T : Any> ScenarioBuilder.givenHttpCall(
    name: String? = null,
    retry: RetryStep? = null,
    noinline h: HttpCallExecutionBuilder<T>.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("HTTP call"),
        retry = retry
    ) { HttpCallExecutionBuilder<T>().apply(h) }