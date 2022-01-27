package com.github.lemfi.kest.executor.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.executor.rabbitmq.model.RabbitMQPublicationProperties
import com.github.lemfi.kest.executor.rabbitmq.model.RabbitMQSnifferProp
import com.rabbitmq.client.*
import org.opentest4j.AssertionFailedError
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.util.*


internal data class RabbitMQMessageExecution(
    val message: String,
    val connection: String,
    val vhost: String,
    val exchange: String,
    val routingKey: String,
    val headers: Map<String, Any>,
    val properties: RabbitMQPublicationProperties?,
    val rabbitMQSnifferProp: RabbitMQSnifferProp,
) : Execution<Unit>() {

    private var messageConsumed = false

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

        val listeningQueue = if (rabbitMQSnifferProp.active) "kest-${UUID.randomUUID()}" else null

        ConnectionFactory().also {

            it.setUri("$connection/$encodedVhost")
        }
            .newConnection("kest connection")
            .createChannel().apply {
                if (rabbitMQSnifferProp.active) queueDeclare(listeningQueue, false, false, true, mutableMapOf())
            }
            .publish(listeningQueue)
            .wait(listeningQueue)

    }

    private fun Channel.publish(replyTo: String?): Channel {

        basicPublish(
            exchange, routingKey, AMQP.BasicProperties().builder()
                .apply {
                    if (replyTo != null) replyTo(replyTo)
                }.apply {
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

    private fun Channel.wait(listeningQueue: String?) {
        if (rabbitMQSnifferProp.active) {

            basicConsume(listeningQueue, false,
                object : DefaultConsumer(this) {
                    override fun handleDelivery(
                        consumerTag: String,
                        envelope: Envelope,
                        properties: AMQP.BasicProperties,
                        body: ByteArray
                    ) {
                        val deliveryTag = envelope.deliveryTag
                        messageConsumed = true
                        channel.basicAck(deliveryTag, false)

                        channel.close()
                    }
                })

            var waiting = rabbitMQSnifferProp.ackTimeout
            while (!messageConsumed && waiting > 0) {
                waiting -= 100
                Thread.sleep(100)
            }
            if (waiting <= 0) throw AssertionFailedError("Message not consumed in time")
        }


    }
}
