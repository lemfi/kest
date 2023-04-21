@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.executor.RabbitMQQueueCreationExecution
import com.github.lemfi.kest.rabbitmq.model.rabbitMQProperty

class RabbitMQQueueCreationExecutionBuilder : ExecutionBuilder<Unit> {

    fun `create queue`(name: () -> String) = QueueAndBinding(name()).also { queue = it }

    @Deprecated("use andBindItToExchange instead", replaceWith = ReplaceWith("this andBindItToExchange exchange"))
    infix fun QueueAndBinding.`and bind it to exchange`(exchange: String) = andBindItToExchange(exchange)
    infix fun QueueAndBinding.andBindItToExchange(exchange: String) = this.also { it.exchange = exchange }

    @Deprecated("use withRoutingKey instead", replaceWith = ReplaceWith("this withRoutingKey routingKey"))
    infix fun QueueAndBinding.`with routing key`(routingKey: String) = withRoutingKey(routingKey)
    infix fun QueueAndBinding.withRoutingKey(routingKey: String) = this.also { it.routingKey = routingKey }

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