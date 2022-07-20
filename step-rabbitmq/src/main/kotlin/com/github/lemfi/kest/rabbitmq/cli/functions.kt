@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.rabbitmq.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQCountMessagesExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQMessageExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQQueueCreationExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQQueueDeletionExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQQueueMultipleMessagesReaderExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQQueueSingleReaderExecutionBuilder
import com.github.lemfi.kest.rabbitmq.model.RabbitMQMessage
import com.github.lemfi.kest.rabbitmq.model.RabbitMQMessageCount

fun ScenarioBuilder.`publish rabbitmq message`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RabbitMQMessageExecutionBuilder.() -> Unit
): StepPostExecution<Unit> {
    val executionBuilder = RabbitMQMessageExecutionBuilder()
    return StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("publish message to rabbitmq"),
        scenarioName = scenarioName,
        retry = retry
    ).addToScenario(executionBuilder, builder)
}

inline fun <reified T> ScenarioBuilder.`given messages from rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    noinline builder: RabbitMQQueueMultipleMessagesReaderExecutionBuilder<T>.() -> Unit
): StepPostExecution<List<RabbitMQMessage<T>>> {
    val executionBuilder = RabbitMQQueueMultipleMessagesReaderExecutionBuilder<T>().apply {
        if (T::class.java == ByteArray::class.java) {
            messageTransformer = { this as T }
        }
    }
    return StandaloneStep<List<RabbitMQMessage<T>>>(
        name = name?.let { StepName(it) } ?: StepName("read message from rabbitmq queue"),
        scenarioName = scenarioName,
        retry = retry
    ).addToScenario(executionBuilder, builder)
        .apply {
            `assert that` {
                eq(
                    executionBuilder.nbMessages,
                    it.size
                ) { "Expected ${executionBuilder.nbMessages} messages in queue, got ${it.size}" }
            }
        }
}

inline fun <reified T> ScenarioBuilder.`given message from rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    noinline builder: RabbitMQQueueSingleReaderExecutionBuilder<T>.() -> Unit
): StepPostExecution<RabbitMQMessage<T>> {
    val executionBuilder = RabbitMQQueueSingleReaderExecutionBuilder<T>().apply {
        if (T::class.java == ByteArray::class.java) {
            messageTransformer = { this as T }
        }
    }
    return StandaloneStep<RabbitMQMessage<T>>(
        name = name?.let { StepName(it) } ?: StepName("read message from rabbitmq queue"),
        scenarioName = scenarioName,
        retry = retry
    ).addToScenario(executionBuilder, builder)
}

@JvmName("readRabbitMQMessageAsByteArray")
fun ScenarioBuilder.`given messages from rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    h: RabbitMQQueueMultipleMessagesReaderExecutionBuilder<ByteArray>.() -> Unit
): StepPostExecution<List<RabbitMQMessage<ByteArray>>> {
    return `given messages from rabbitmq queue`<ByteArray>(name, retry, h)
}

fun ScenarioBuilder.`create rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RabbitMQQueueCreationExecutionBuilder.() -> Unit
): StepPostExecution<Unit> {
    val executionBuilder = RabbitMQQueueCreationExecutionBuilder()
    return StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("create rabbitmq queue"),
        scenarioName = scenarioName,
        retry = retry
    ).addToScenario(executionBuilder, builder)
}

fun ScenarioBuilder.`given number of messages in rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RabbitMQCountMessagesExecutionBuilder.() -> Unit
): StepPostExecution<RabbitMQMessageCount> {
    val executionBuilder = RabbitMQCountMessagesExecutionBuilder()
    return StandaloneStep<RabbitMQMessageCount>(
        name = name?.let { StepName(it) } ?: StepName("count messages from rabbitmq queue"),
        scenarioName = scenarioName,
        retry = retry
    ).addToScenario(executionBuilder, builder)
}

fun ScenarioBuilder.`delete rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RabbitMQQueueDeletionExecutionBuilder.() -> Unit
): StepPostExecution<Unit> {
    val executionBuilder = RabbitMQQueueDeletionExecutionBuilder()
    return StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("delete rabbitmq queue"),
        scenarioName = scenarioName,
        retry = retry
    ).addToScenario(executionBuilder, builder)
}