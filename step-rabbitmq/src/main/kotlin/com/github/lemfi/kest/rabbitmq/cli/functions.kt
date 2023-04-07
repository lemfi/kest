@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.rabbitmq.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.model.DefaultStepName
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQCountMessagesExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQMessageExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQQueueCreationExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQQueueDeletionExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQQueueMultipleMessagesReaderExecutionBuilder
import com.github.lemfi.kest.rabbitmq.builder.RabbitMQQueueSingleReaderExecutionBuilder

fun ScenarioBuilder.`publish rabbitmq message`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RabbitMQMessageExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("publish message to rabbitmq"),
        retry = retry
    ) { RabbitMQMessageExecutionBuilder().apply(builder) }

inline fun <reified T> ScenarioBuilder.`given messages from rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    noinline builder: RabbitMQQueueMultipleMessagesReaderExecutionBuilder<T>.() -> Unit
) =
    RabbitMQQueueMultipleMessagesReaderExecutionBuilder<T>().apply {
        if (T::class.java == ByteArray::class.java) {
            messageTransformer = { this as T }
        }
    }.let { executionBuilder ->

        createStep(
            name = name?.let { StepName(it) } ?: DefaultStepName("read message from rabbitmq queue"),
            retry = retry
        ) {
            executionBuilder.apply(builder)
        }
            .apply {
                `assert that` {
                    it.size isEqualTo executionBuilder.nbMessages { "Expected ${executionBuilder.nbMessages} messages in queue, got ${it.size}" }
                }
            }
    }

inline fun <reified T> ScenarioBuilder.`given message from rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    noinline builder: RabbitMQQueueSingleReaderExecutionBuilder<T>.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("read message from rabbitmq queue"),
        retry = retry
    ) {
        RabbitMQQueueSingleReaderExecutionBuilder<T>().apply {
            if (T::class.java == ByteArray::class.java) {
                messageTransformer = { this as T }
            }
        }.apply(builder)
    }

@JvmName("readRabbitMQMessageAsByteArray")
fun ScenarioBuilder.`given messages from rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    h: RabbitMQQueueMultipleMessagesReaderExecutionBuilder<ByteArray>.() -> Unit
) = `given messages from rabbitmq queue`<ByteArray>(name, retry, h)


fun ScenarioBuilder.`create rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RabbitMQQueueCreationExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("create rabbitmq queue"),
        retry = retry
    ) { RabbitMQQueueCreationExecutionBuilder().apply(builder) }

fun ScenarioBuilder.`given number of messages in rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RabbitMQCountMessagesExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("count messages from rabbitmq queue"),
        retry = retry
    ) { RabbitMQCountMessagesExecutionBuilder().apply(builder) }

fun ScenarioBuilder.`delete rabbitmq queue`(
    name: String? = null,
    retry: RetryStep? = null,
    builder: RabbitMQQueueDeletionExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("delete rabbitmq queue"),
        retry = retry
    ) { RabbitMQQueueDeletionExecutionBuilder().apply(builder) }