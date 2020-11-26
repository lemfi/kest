package com.github.lemfi.kest.executor.rabbitmq.model

import com.github.lemfi.kest.core.properties.property
import org.slf4j.LoggerFactory

data class RabbitMQProperties(
        val rabbitmq: RabbitMQProp
)

data class RabbitMQProp(
        val protocol: String = "amqp",
        val host: String = "localhost",
        val port: Int = 5672,
        val user: String = "guest",
        val password: String = "guest",
        val vhost: String = "/",
        val exchange: String = "",
        val timeout: Long = 2000,
        val consumedMessageListener: RabbitMQSnifferProp = RabbitMQSnifferProp(),
)

data class RabbitMQSnifferProp(
        val active: Boolean = false,
        val startedByKest: Boolean = true,
        val port: Int = 5673,
)

inline fun <R> rabbitMQProperty(crossinline l: RabbitMQProp.()->R): R {
    val shortcut: RabbitMQProperties.()->R = { rabbitmq.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("RABBITMQ-Kest").error("No configuration found for rabbitmq", e)
        throw e
    }
}