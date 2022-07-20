package com.github.lemfi.kest.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.model.RabbitMQMessage

internal class RabbitMQSingleMessagesQueueReaderExecution<T>(
    queueName: String,
    deleteQueue: Boolean,
    connection: String,
    vhost: String,
    l: ByteArray.() -> T,
) : Execution<RabbitMQMessage<T>>() {

    val execution = RabbitMQMultipleMessagesQueueReaderExecution(queueName, deleteQueue, connection, vhost, 1, l)

    override fun onAssertionFailedError() {
        execution.onAssertionFailedError()
    }

    override fun onAssertionSuccess() {
        execution.onAssertionSuccess()
    }

    override fun onExecutionEnded() {
        execution.onExecutionEnded()
    }

    override fun execute(): RabbitMQMessage<T> {
        return execution
            .execute()
            .first()
    }
}
