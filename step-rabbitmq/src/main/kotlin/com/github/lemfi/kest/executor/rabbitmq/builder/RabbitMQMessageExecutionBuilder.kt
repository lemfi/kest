package com.github.lemfi.kest.executor.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.executor.rabbitmq.executor.RabbitMQMessageExecution
import com.github.lemfi.kest.executor.rabbitmq.model.rabbitMQProperty

class RabbitMQMessageExecutionBuilder : ExecutionBuilder<Unit> {

    fun publish(message: ()->String) = RabbitMQMessage(message()).also { this.message = it }
    infix fun RabbitMQMessage.`to exchange`(exchange: String) = also { it.exchange = exchange }
    infix fun RabbitMQMessage.`with routing key`(routingKey: String?) = also { it.routingKey = routingKey }

    private lateinit var message: RabbitMQMessage

    var connection = rabbitMQProperty { connection }
    var vhost = rabbitMQProperty { vhost }


    override fun toExecution(): Execution<Unit> {
        return RabbitMQMessageExecution(
            message.message,
            connection,
            vhost,
            message.exchange ?: rabbitMQProperty { exchange },
            requireNotNull(message.routingKey) { "please give a routing key for publishing a message" } ,
            rabbitMQProperty { rabbitProxy },
        )
    }
}
data class RabbitMQMessage(
    val message: String,
    var routingKey: String? = null,
    var exchange: String? = null
)