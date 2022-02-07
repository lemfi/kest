@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.executor.RabbitMQQueueCreationExecution
import com.github.lemfi.kest.rabbitmq.model.rabbitMQProperty

class RabbitMQQueueCreationExecutionBuilder : ExecutionBuilder<Unit> {

    fun `create queue`(name: () -> String) = QueueAndBinding(name()).also { queue = it }
    infix fun QueueAndBinding.`and bind it to exchange`(exchange: String) = this.also { it.exchange = exchange }
    infix fun QueueAndBinding.`with routing key`(routingKey: String) = this.also { it.routingKey = routingKey }

    private lateinit var queue: QueueAndBinding

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = rabbitMQProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var vhost = rabbitMQProperty { vhost }

    override fun toExecution(): Execution<Unit> {
        return RabbitMQQueueCreationExecution(
            queue, connection, vhost,
        )
    }
}

data class QueueAndBinding(val queue: String, var exchange: String? = null, var routingKey: String? = null)