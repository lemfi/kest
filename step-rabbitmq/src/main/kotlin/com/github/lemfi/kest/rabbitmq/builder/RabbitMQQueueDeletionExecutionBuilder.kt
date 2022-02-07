@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.executor.RabbitMQQueueDeletionExecution
import com.github.lemfi.kest.rabbitmq.model.rabbitMQProperty

class RabbitMQQueueDeletionExecutionBuilder : ExecutionBuilder<Unit> {

    lateinit var queue: String

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = rabbitMQProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var vhost = rabbitMQProperty { vhost }

    override fun toExecution(): Execution<Unit> {
        return RabbitMQQueueDeletionExecution(
            queue, connection, vhost,
        )
    }
}