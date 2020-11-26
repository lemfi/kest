package com.github.lemfi.kest.executor.http.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.executor.http.builder.HttpCallExecutionBuilder
import com.github.lemfi.kest.executor.http.model.HttpResponse

inline fun <reified T> ScenarioBuilder.`given http call`(crossinline h: HttpCallExecutionBuilder<T>.()->Unit): Step<HttpResponse<T>> {
    return Step(HttpCallExecutionBuilder(T::class.java).apply(h).build()).apply {
        steps.add(this)
    }
}