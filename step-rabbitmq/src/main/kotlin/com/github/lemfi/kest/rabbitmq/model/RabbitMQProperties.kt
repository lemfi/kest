package com.github.lemfi.kest.rabbitmq.model

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.properties.property

internal data class RabbitMQProperties(
    val rabbitmq: RabbitMQProp
)

internal data class RabbitMQProp(
    val connection: String = "amqp://guest:guest@localhost:5672",
    val managementapi: RabbitMQManagementApiProp = RabbitMQManagementApiProp(),
    val vhost: String = "/",
    val exchange: String = "",
)

internal data class RabbitMQManagementApiProp(
    val connection: String = "http://localhost:15672",
    val user: String = "guest",
    val password: String = "guest"
)

internal fun <R> rabbitMQProperty(l: RabbitMQProp.() -> R): R {
    val shortcut: RabbitMQProperties.() -> R = { rabbitmq.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("RABBITMQ-Kest").debug("No configuration found for rabbitmq, use default values")
        RabbitMQProp().l()
    }
}