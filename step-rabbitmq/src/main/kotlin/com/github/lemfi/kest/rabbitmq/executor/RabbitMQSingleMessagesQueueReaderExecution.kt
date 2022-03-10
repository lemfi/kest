package com.github.lemfi.kest.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.model.RabbitMQMessage

internal class RabbitMQSingleMessagesQueueReaderExecution<T>(
    private val queueName: String,
    private val deleteQueue: Boolean,
    private val connection: String,
    private val vhost: String,
    private val l: ByteArray.() -> T,
) : Execution<RabbitMQMessage<T>>() {

    override fun execute(): RabbitMQMessage<T> {
        return RabbitMQMultipleMessagesQueueReaderExecution(queueName, deleteQueue, connection, vhost, 1, l)
            .execute()
            .first()
    }
}
