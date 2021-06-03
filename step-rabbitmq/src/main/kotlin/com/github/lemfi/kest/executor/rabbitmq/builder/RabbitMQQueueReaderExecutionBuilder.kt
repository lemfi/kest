package com.github.lemfi.kest.executor.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.ExecutionDescription
import com.github.lemfi.kest.executor.rabbitmq.executor.RabbitMQQueueReaderExecution
import com.github.lemfi.kest.executor.rabbitmq.model.rabbitMQProperty

class RabbitMQQueueReaderExecutionBuilder<T> : ExecutionBuilder<T> {

    private var description: ExecutionDescription? = null
    fun description(l: ()->String) {
        description = ExecutionDescription(l())
    }

    lateinit var queue: String
    lateinit var messageTransformer: ByteArray.() -> T
    var deleteQueue = false

    var connection = rabbitMQProperty { connection }
    var vhost = rabbitMQProperty { vhost }

    override fun toExecution(): Execution<T> {
        return RabbitMQQueueReaderExecution(
            description, queue, deleteQueue, connection, vhost, messageTransformer
        )
    }
}