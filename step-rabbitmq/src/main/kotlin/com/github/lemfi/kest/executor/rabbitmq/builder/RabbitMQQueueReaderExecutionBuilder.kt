package com.github.lemfi.kest.executor.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.executor.rabbitmq.executor.RabbitMQQueueReaderExecution
import com.github.lemfi.kest.executor.rabbitmq.model.RabbitMQMessage
import com.github.lemfi.kest.executor.rabbitmq.model.rabbitMQProperty

class RabbitMQQueueReaderExecutionBuilder<T> : ExecutionBuilder<List<RabbitMQMessage<T>>> {

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
        return RabbitMQQueueReaderExecution(
            queue, deleteQueue, connection, vhost, nbMessages, messageTransformer
        )
    }
}