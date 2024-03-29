package com.github.lemfi.kest.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.executor.RabbitMQMultipleMessagesQueueReaderExecution
import com.github.lemfi.kest.rabbitmq.model.RabbitMQMessage
import com.github.lemfi.kest.rabbitmq.model.rabbitMQProperty

class RabbitMQQueueMultipleMessagesReaderExecutionBuilder<T> : ExecutionBuilder<List<RabbitMQMessage<T>>> {

    lateinit var queue: String

    var nbMessages: Int = 1

    lateinit var messageTransformer: ByteArray.() -> T

    @Suppress("MemberVisibilityCanBePrivate")
    var deleteQueue = false

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = rabbitMQProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var vhost = rabbitMQProperty { vhost }

    override fun toExecution(): Execution<List<RabbitMQMessage<T>>> {
        return RabbitMQMultipleMessagesQueueReaderExecution(
            queue, deleteQueue, connection, vhost, nbMessages, messageTransformer
        )
    }
}