@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.redis.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution
import com.github.lemfi.kest.redis.builder.RedisDeleteExecutionBuilder
import com.github.lemfi.kest.redis.builder.RedisInsertExecutionBuilder
import com.github.lemfi.kest.redis.builder.RedisReadExecutionBuilder

fun ScenarioBuilder.`redis delete key`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RedisDeleteExecutionBuilder.() -> Unit
) {
    val executionBuilder = RedisDeleteExecutionBuilder()
    StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("delete redis key"),
        scenarioName = this.name,
        retry = retry
    ).addToScenario(executionBuilder, builder)
}

fun ScenarioBuilder.`redis get key`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RedisReadExecutionBuilder.() -> Unit
): StepPostExecution<String?> {
    val executionBuilder = RedisReadExecutionBuilder()
    return StandaloneStep<String?>(
        name = name?.let { StepName(it) } ?: StepName("get redis key"),
        scenarioName = this.name,
        retry = retry
    ).addToScenario(executionBuilder, builder)
}

fun ScenarioBuilder.`redis insert data`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RedisInsertExecutionBuilder.() -> Unit
) {
    val executionBuilder = RedisInsertExecutionBuilder()
    StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("insert data in redis"),
        scenarioName = this.name,
        retry = retry
    ).addToScenario(executionBuilder, builder)
}