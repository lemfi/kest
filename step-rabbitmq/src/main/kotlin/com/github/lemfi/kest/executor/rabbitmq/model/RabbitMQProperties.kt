package com.github.lemfi.kest.executor.rabbitmq.model

import com.github.lemfi.kest.core.properties.property
import org.slf4j.LoggerFactory

data class RabbitMQProperties(
    val rabbitmq: RabbitMQProp
)

data class RabbitMQProp(
    val connection: String = "amqp://guest:guest@localhost:5672",
    val vhost: String = "/",
    val exchange: String = "",
    val rabbitProxy: RabbitMQSnifferProp = RabbitMQSnifferProp(),
)

data class RabbitMQSnifferProp(
    val active: Boolean = false,
    val ackTimeout: Long = 10000,
    val port: Int = 5673,
)

fun <R> rabbitMQProperty(l: RabbitMQProp.() -> R): R {
    val shortcut: RabbitMQProperties.() -> R = { rabbitmq.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("RABBITMQ-Kest").warn("No configuration found for rabbitmq, use default values")
        RabbitMQProp().l()
    }
}