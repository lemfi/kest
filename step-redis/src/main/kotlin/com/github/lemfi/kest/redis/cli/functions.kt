@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.redis.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.DefaultStepName
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.redis.builder.RedisDeleteExecutionBuilder
import com.github.lemfi.kest.redis.builder.RedisInsertExecutionBuilder
import com.github.lemfi.kest.redis.builder.RedisReadExecutionBuilder

@Deprecated("use redisDeleteKey instead")
fun ScenarioBuilder.`redis delete key`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RedisDeleteExecutionBuilder.() -> Unit
) = redisDeleteKey(name, retry, builder)

fun ScenarioBuilder.redisDeleteKey(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RedisDeleteExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("delete redis key"),
        retry = retry,
    ) {
        RedisDeleteExecutionBuilder().apply(builder)
    }

@Deprecated("use redisGetKey instead")
fun ScenarioBuilder.`redis get key`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RedisReadExecutionBuilder.() -> Unit
) = redisGetKey(name, retry, builder)

fun ScenarioBuilder.redisGetKey(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RedisReadExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("get redis key"),
        retry = retry,
    ) {
        RedisReadExecutionBuilder().apply(builder)
    }

@Deprecated("use redisInsertData instead")
fun ScenarioBuilder.`redis insert data`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RedisInsertExecutionBuilder.() -> Unit
) = redisInsertData(name, retry, builder)

fun ScenarioBuilder.redisInsertData(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RedisInsertExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("insert data in redis"),
        retry = retry,
    ) {
        RedisInsertExecutionBuilder().apply(builder)
    }
