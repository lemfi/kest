package com.github.lemfi.kest.executor.rabbitmq.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution
import com.github.lemfi.kest.executor.rabbitmq.builder.RabbitMQMessageExecutionBuilder
import com.github.lemfi.kest.executor.rabbitmq.builder.RabbitMQQueueCreationExecutionBuilder
import com.github.lemfi.kest.executor.rabbitmq.builder.RabbitMQQueueReaderExecutionBuilder

inline fun ScenarioBuilder.`publish rabbitmq message`(
    name: String? = null,
    retryStep: RetryStep? = null,
    crossinline h: RabbitMQMessageExecutionBuilder.() -> Unit
): StepPostExecution<Unit> {
    return Step(
        name = name?.let { StepName(it) } ?: StepName("publish message to rabbitmq"),
        scenarioName = this.name!!,
        execution = { RabbitMQMessageExecutionBuilder().apply(h).build() },
        retry = retryStep
    )
        .apply { steps.add(this) }
        .postExecution
}

inline fun <reified T> ScenarioBuilder.`given message from rabbitmq queue`(
    name: String? = null,
    retryStep: RetryStep? = null,
    crossinline h: RabbitMQQueueReaderExecutionBuilder<T>.() -> Unit
): StepPostExecution<T> {
    return Step(
        name = name?.let { StepName(it) } ?: StepName("read message from rabbitmq queue"),
        scenarioName = this.name!!,
        execution = {
            RabbitMQQueueReaderExecutionBuilder<T>().apply {
                if (T::class.java == ByteArray::class.java) {
                    messageTransformer = { this as T }
                }
            }.apply(h).build()
        },
        retry = retryStep
    )
        .apply { steps.add(this) }
        .postExecution
}

@JvmName("readRabbitMQMessageAsByteArray")
inline fun ScenarioBuilder.`given message from rabbitmq queue`(
    name: String? = null,
    retryStep: RetryStep? = null,
    crossinline h: RabbitMQQueueReaderExecutionBuilder<ByteArray>.() -> Unit
): StepPostExecution<ByteArray> {
    return `given message from rabbitmq queue`<ByteArray>(name, retryStep, h)
}

inline fun ScenarioBuilder.`create rabbitmq queue`(
    name: String? = null,
    retryStep: RetryStep? = null,
    crossinline h: RabbitMQQueueCreationExecutionBuilder.() -> Unit
): StepPostExecution<Unit> {
    return Step(
        name = name?.let { StepName(it) } ?: StepName("create rabbitmq queue"),
        scenarioName = this.name!!,
        execution = { RabbitMQQueueCreationExecutionBuilder().apply(h).build() },
        retry = retryStep
    )
        .apply { steps.add(this) }
        .postExecution
}