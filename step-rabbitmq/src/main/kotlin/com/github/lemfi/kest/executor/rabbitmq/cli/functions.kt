package com.github.lemfi.kest.executor.rabbitmq.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.executor.rabbitmq.builder.RabbitMQMessageExecutionBuilder
import com.github.lemfi.kest.executor.rabbitmq.builder.RabbitMQQueueReaderExecutionBuilder

inline fun ScenarioBuilder.`publish rabbitmq message`(crossinline h: RabbitMQMessageExecutionBuilder.()->Unit): Step<Unit> {
    return Step(RabbitMQMessageExecutionBuilder().apply(h).build()).apply {
        steps.add(this)
    }
}

inline fun <reified T> ScenarioBuilder.`given message from rabbitmq queue`(crossinline h: RabbitMQQueueReaderExecutionBuilder<T>.()->Unit): Step<T> {
    return Step(RabbitMQQueueReaderExecutionBuilder<T>().apply {
        if (T::class.java == ByteArray::class.java) {
            messageTransformer = { this as T }
        }
    }.apply(h).build()).apply {
        steps.add(this)
    }
}

@JvmName("readRabbitMQMessageAsByteArray")
inline fun ScenarioBuilder.`given message from rabbitmq queue`(crossinline h: RabbitMQQueueReaderExecutionBuilder<ByteArray>.()->Unit): Step<ByteArray> {
    return `given message from rabbitmq queue`<ByteArray>(h)
}