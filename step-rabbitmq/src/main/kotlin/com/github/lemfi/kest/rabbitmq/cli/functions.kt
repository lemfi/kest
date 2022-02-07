@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.rabbitmq.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQMessageExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQQueueCreationExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQQueueDeletionExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQQueueReaderExecutionBuilder
import com.github.lemfi.kest.rabbitmq.model.RabbitMQMessage

fun ScenarioBuilder.`publish rabbitmq message`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RabbitMQMessageExecutionBuilder.() -> Unit
): StepPostExecution<Unit> {
    val executionBuilder = RabbitMQMessageExecutionBuilder()
    return StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("publish message to rabbitmq"),
        scenarioName = this.name,
        retry = retry
    ).addToScenario(executionBuilder, builder)
}

inline fun <reified T> ScenarioBuilder.`given messages from rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    noinline builder: RabbitMQQueueReaderExecutionBuilder<T>.() -> Unit
): StepPostExecution<List<RabbitMQMessage<T>>> {
    val executionBuilder = RabbitMQQueueReaderExecutionBuilder<T>().apply {
        if (T::class.java == ByteArray::class.java) {
            messageTransformer = { this as T }
        }
    }
    return StandaloneStep<List<RabbitMQMessage<T>>>(
        name = name?.let { StepName(it) } ?: StepName("read message from rabbitmq queue"),
        scenarioName = this.name,
        retry = retry
    ).addToScenario(executionBuilder, builder)
}

inline fun <reified T> ScenarioBuilder.`given message from rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    noinline builder: RabbitMQQueueReaderExecutionBuilder<T>.() -> Unit
): StepPostExecution<RabbitMQMessage<T>> {
    val executionBuilder = RabbitMQQueueReaderExecutionBuilder<T>().apply {
        if (T::class.java == ByteArray::class.java) {
            messageTransformer = { this as T }
        }
    }
    return StandaloneStep<List<RabbitMQMessage<T>>>(
        name = name?.let { StepName(it) } ?: StepName("read message from rabbitmq queue"),
        scenarioName = this.name,
        retry = retry
    ).let { step ->

        step
            .addToScenario(executionBuilder, builder)
            .`map result to` { it.first() }

        StepPostExecution(step, null) { t -> t }
    }
}

@JvmName("readRabbitMQMessageAsByteArray")
fun ScenarioBuilder.`given messages from rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    h: RabbitMQQueueReaderExecutionBuilder<ByteArray>.() -> Unit
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
        scenarioName = this.name,
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
        scenarioName = this.name,
        retry = retry
    ).addToScenario(executionBuilder, builder)
}