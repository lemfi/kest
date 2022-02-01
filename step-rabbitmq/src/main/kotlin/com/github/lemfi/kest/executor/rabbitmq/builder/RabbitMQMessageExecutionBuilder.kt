@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.executor.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.executor.rabbitmq.executor.RabbitMQMessageExecution
import com.github.lemfi.kest.executor.rabbitmq.model.RabbitMQPublicationProperties
import com.github.lemfi.kest.executor.rabbitmq.model.rabbitMQProperty

class RabbitMQMessageExecutionBuilder : ExecutionBuilder<Unit> {

    fun publish(message: () -> String) = RabbitMQMessage(message()).also { this.message = it }
    infix fun RabbitMQMessage.`to exchange`(exchange: String) = also { it.exchange = exchange }
    infix fun RabbitMQMessage.`with routing key`(routingKey: String?) = also { it.routingKey = routingKey }
    infix fun RabbitMQMessage.`with headers`(headers: Map<String, Any>) = also { it.headers = headers }
    infix fun RabbitMQMessage.`with properties`(propertiesBuilder: RabbitMQPropertiesBuilder.() -> Unit) =
        also { it.properties = RabbitMQPropertiesBuilder().apply(propertiesBuilder).build() }

    private lateinit var message: RabbitMQMessage

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = rabbitMQProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var vhost = rabbitMQProperty { vhost }


    override fun toExecution(): Execution<Unit> {
        return RabbitMQMessageExecution(
            message.message,
            connection,
            vhost,
            message.exchange ?: rabbitMQProperty { exchange },
            requireNotNull(message.routingKey) { "please give a routing key for publishing a message" },
            message.headers,
            message.properties,
        )
    }
}

data class RabbitMQMessage(
    val message: String,
    var routingKey: String? = null,
    var exchange: String? = null,
    var headers: Map<String, Any> = mapOf(),
    var properties: RabbitMQPublicationProperties? = null,
)

class RabbitMQPropertiesBuilder {
    var contentType: String? = null
    var contentEncoding: String? = null
    var deliveryMode: Int? = null
    var priority: Int? = null
    var correlationId: String? = null
    var replyTo: String? = null

    fun build() =
        RabbitMQPublicationProperties(contentType, contentEncoding, deliveryMode, priority, correlationId, replyTo)
}