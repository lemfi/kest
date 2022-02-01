package com.github.lemfi.kest.executor.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.executor.rabbitmq.model.RabbitMQPublicationProperties
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
            |message: $message 
            |on exchange: 
            |          exchange: ${exchange.ifBlank { """default ("")""" }}
            |          routing key: $routingKey"""
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
                        properties.priority?.also { priority(it) }
                        properties.replyTo?.also { replyTo(it) }
                        properties.correlationId?.also { correlationId(it) }
                    }
                }
                .headers(headers)
                .build(), message.toByteArray(Charsets.UTF_8)
        )

        return this
    }
}
