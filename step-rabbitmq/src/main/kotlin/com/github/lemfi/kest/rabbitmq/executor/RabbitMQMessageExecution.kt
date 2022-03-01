package com.github.lemfi.kest.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.model.RabbitMQPublicationProperties
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import org.slf4j.LoggerFactory
import java.net.URLEncoder


internal data class RabbitMQMessageExecution(
    val message: String,
    val connection: String,
    val vhost: String,
    val exchange: String,
    val routingKey: String,
    val headers: Map<String, Any>,
    val properties: RabbitMQPublicationProperties?,
) : Execution<Unit>() {

    private val encodedVhost: String = URLEncoder.encode(vhost, Charsets.UTF_8)
    override fun execute() {

        LoggerFactory.getLogger("RABBITMQ-Kest").info(
            """
            |Write message:
            |
            |vhost: $vhost 
            |exchange: ${exchange.ifBlank { """default ("")""" }}
            |routing key: $routingKey
            |headers: 
            |    ${headers.map { "${it.key}=${it.value}" }.joinToString("\n    ")}
            |properties: 
            |    ${properties ?: ""}
            |
            |message: $message 
            |"""
                .trimMargin()
        )

        ConnectionFactory().also {

            it.setUri("$connection/$encodedVhost")
        }
            .newConnection("kest connection")
            .createChannel()
            .publish()

    }

    private fun Channel.publish(): Channel {

        basicPublish(
            exchange, routingKey, AMQP.BasicProperties().builder()
                .apply {
                    if (properties != null) {
                        properties.contentType?.also { contentType(it) }
                        properties.deliveryMode?.also { deliveryMode(it) }
                        properties.contentEncoding?.also { contentEncoding(it) }
                        properties.userId?.also { userId(it) }
                        properties.appId?.also { appId(it) }
                        properties.replyTo?.also { replyTo(it) }
                        properties.correlationId?.also { correlationId(it) }
                        properties.timestamp?.also { timestamp(it) }
                        properties.expiration?.also { expiration(it) }
                        properties.messageId?.also { messageId(it) }
                        properties.type?.also { type(it) }
                    }
                }
                .headers(headers)
                .build(), message.toByteArray(Charsets.UTF_8)
        )

        return this
    }
}
