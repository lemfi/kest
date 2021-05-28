package com.github.lemfi.kest.executor.rabbitmq.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.cli.addToScenario
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution
import com.github.lemfi.kest.executor.rabbitmq.builder.RabbitMQMessageExecutionBuilder
import com.github.lemfi.kest.executor.rabbitmq.builder.RabbitMQQueueCreationExecutionBuilder
import com.github.lemfi.kest.executor.rabbitmq.builder.RabbitMQQueueReaderExecutionBuilder

fun ScenarioBuilder.`publish rabbitmq message`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: RabbitMQMessageExecutionBuilder.() -> Unit
) {
    val executionBuilder = RabbitMQMessageExecutionBuilder()
    StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("publish message to rabbitmq"),
        scenarioName = this.name!!,
        retry = retryStep
    ).addToScenario(this, executionBuilder, h)
}

inline fun <reified T> ScenarioBuilder.`given message from rabbitmq queue`(
    name: String? = null,
    retryStep: RetryStep? = null,
    noinline h: RabbitMQQueueReaderExecutionBuilder<T>.() -> Unit
): StepPostExecution<T> {
    val executionBuilder = RabbitMQQueueReaderExecutionBuilder<T>().apply {
        if (T::class.java == ByteArray::class.java) {
            messageTransformer = { this as T }
        }
    }
    return StandaloneStep<T>(
        name = name?.let { StepName(it) } ?: StepName("read message from rabbitmq queue"),
        scenarioName = this.name!!,
        retry = retryStep
    ).addToScenario(this, executionBuilder, h)
}

@JvmName("readRabbitMQMessageAsByteArray")
fun ScenarioBuilder.`given message from rabbitmq queue`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: RabbitMQQueueReaderExecutionBuilder<ByteArray>.() -> Unit
): StepPostExecution<ByteArray> {
    return `given message from rabbitmq queue`<ByteArray>(name, retryStep, h)
}

fun ScenarioBuilder.`create rabbitmq queue`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: RabbitMQQueueCreationExecutionBuilder.() -> Unit
) {
    val executionBuilder = RabbitMQQueueCreationExecutionBuilder()
    StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("create rabbitmq queue"),
        scenarioName = this.name!!,
        retry = retryStep
    ).addToScenario(this, executionBuilder, h)
}