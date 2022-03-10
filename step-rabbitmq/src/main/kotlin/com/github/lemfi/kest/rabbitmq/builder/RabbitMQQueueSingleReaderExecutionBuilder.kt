package com.github.lemfi.kest.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.executor.RabbitMQSingleMessagesQueueReaderExecution
import com.github.lemfi.kest.rabbitmq.model.RabbitMQMessage
import com.github.lemfi.kest.rabbitmq.model.rabbitMQProperty

class RabbitMQQueueSingleReaderExecutionBuilder<T> : ExecutionBuilder<RabbitMQMessage<T>> {

    lateinit var queue: String

    lateinit var messageTransformer: ByteArray.() -> T

    @Suppress("MemberVisibilityCanBePrivate")
    var deleteQueue = false

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = rabbitMQProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var vhost = rabbitMQProperty { vhost }

    override fun toExecution(): Execution<RabbitMQMessage<T>> {
        return RabbitMQSingleMessagesQueueReaderExecution(
            queue, deleteQueue, connection, vhost, messageTransformer
        )
    }
}