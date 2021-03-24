package com.github.lemfi.kest.executor.rabbitmq.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.executor.rabbitmq.builder.RabbitMQMessageExecutionBuilder
import com.github.lemfi.kest.executor.rabbitmq.builder.RabbitMQQueueCreationExecutionBuilder
import com.github.lemfi.kest.executor.rabbitmq.builder.RabbitMQQueueReaderExecutionBuilder

inline fun ScenarioBuilder.`publish rabbitmq message`(retryStep: RetryStep? = null, crossinline h: RabbitMQMessageExecutionBuilder.()->Unit): Step<Unit> {
    return Step({RabbitMQMessageExecutionBuilder().apply(h).build()}, retry = retryStep).apply {
        steps.add(this)
    }
}

inline fun <reified T> ScenarioBuilder.`given message from rabbitmq queue`(retryStep: RetryStep?, crossinline h: RabbitMQQueueReaderExecutionBuilder<T>.()->Unit): Step<T> {
    return Step({RabbitMQQueueReaderExecutionBuilder<T>().apply {
        if (T::class.java == ByteArray::class.java) {
            messageTransformer = { this as T }
        }
    }.apply(h).build()}, retry = retryStep).apply {
        steps.add(this)
    }
}

@JvmName("readRabbitMQMessageAsByteArray")
inline fun ScenarioBuilder.`given message from rabbitmq queue`(retryStep: RetryStep? = null, crossinline h: RabbitMQQueueReaderExecutionBuilder<ByteArray>.()->Unit): Step<ByteArray> {
    return `given message from rabbitmq queue`<ByteArray>(retryStep, h)
}

inline fun ScenarioBuilder.`create rabbitmq queue`(retryStep: RetryStep? = null, crossinline h: RabbitMQQueueCreationExecutionBuilder.()->Unit): Step<Unit> {
    return Step({RabbitMQQueueCreationExecutionBuilder().apply(h).build()}, retry = retryStep).apply {
        steps.add(this)
    }
}