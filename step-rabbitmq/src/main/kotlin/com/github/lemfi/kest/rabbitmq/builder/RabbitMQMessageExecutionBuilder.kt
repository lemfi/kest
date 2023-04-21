@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.executor.RabbitMQMessageExecution
import com.github.lemfi.kest.rabbitmq.model.RabbitMQPublicationProperties
import com.github.lemfi.kest.rabbitmq.model.rabbitMQProperty
import java.util.Date

class RabbitMQMessageExecutionBuilder : ExecutionBuilder<Unit> {

    fun publish(message: () -> String) = RabbitMQMessage(message()).also { this.message = it }

    @Deprecated("use toExchange instead", replaceWith = ReplaceWith("this toExchange exchange"))
    infix fun RabbitMQMessage.`to exchange`(exchange: String) = toExchange(exchange)
    infix fun RabbitMQMessage.toExchange(exchange: String) = also { it.exchange = exchange }

    @Deprecated("use withRoutingKey instead", replaceWith = ReplaceWith("this withRoutingKey routingKey"))
    infix fun RabbitMQMessage.`with routing key`(routingKey: String?) = withRoutingKey(routingKey)
    infix fun RabbitMQMessage.withRoutingKey(routingKey: String?) = also { it.routingKey = routingKey }

    @Deprecated("use withHeaders instead", replaceWith = ReplaceWith("this withHeaders headers"))
    infix fun RabbitMQMessage.`with headers`(headers: Map<String, Any>) = withHeaders(headers)
    infix fun RabbitMQMessage.withHeaders(headers: Map<String, Any>) = also { it.headers = headers }
    @Deprecated("use withProperties instead")
    infix fun RabbitMQMessage.`with properties`(propertiesBuilder: RabbitMQPropertiesBuilder.() -> Unit) =
        withProperties(propertiesBuilder)

    infix fun RabbitMQMessage.withProperties(propertiesBuilder: RabbitMQPropertiesBuilder.() -> Unit) =
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

    var type: String? = null
    var messageId: String? = null
    var expiration: String? = null
    var timestamp: Date? = null
    var userId: String? = null
    var appId: String? = null

    fun build() =
        RabbitMQPublicationProperties(
            deliveryMode = deliveryMode,
            type = type,
            contentType = contentType,
            contentEncoding = contentEncoding,
            messageId = messageId,
            correlationId = correlationId,
            replyTo = replyTo,
            expiration = expiration,
            timestamp = timestamp,
            userId = userId,
            appId = appId
        )
}