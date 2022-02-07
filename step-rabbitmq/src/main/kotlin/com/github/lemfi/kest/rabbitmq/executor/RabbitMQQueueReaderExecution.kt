package com.github.lemfi.kest.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.model.RabbitMQMessage
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.GetResponse
import org.opentest4j.AssertionFailedError
import org.slf4j.LoggerFactory
import java.net.URLEncoder

internal class RabbitMQQueueReaderExecution<T>(
    private val queueName: String,
    private val deleteQueue: Boolean,
    private val connection: String,
    private val vhost: String,
    private val nbMessages: Int,
    val l: ByteArray.() -> T,
) : Execution<List<RabbitMQMessage<T>>>() {

    private val encodedVhost: String = URLEncoder.encode(vhost, Charsets.UTF_8)

    override fun execute(): List<RabbitMQMessage<T>> {

        LoggerFactory.getLogger("RABBITMQ-Kest").info(
            """
            |Read message:
            |
            |vhost: $vhost
            |queue: $queueName"""
                .trimMargin()
        )

        return ConnectionFactory().also {

            it.setUri("$connection/$encodedVhost")
        }
            .newConnection("kest connection")
            .createChannel().run {
                mutableListOf<GetResponse?>().apply {
                    (1..nbMessages).forEach { _ ->
                        add(basicGet(queueName, false))
                    }
                }
                    .let {
                        if (it.contains(null)) {
                            it.onEach { message ->
                                if (message != null) basicNack(message.envelope.deliveryTag, true, true)
                            }
                            throw AssertionFailedError("No message to read")
                        } else it
                    }
                    .filterNotNull()
                    .map {
                        try {
                            it.body?.l()
                                ?.let { body ->
                                    RabbitMQMessage(
                                        message = body as T,
                                        routingKey = it.envelope.routingKey,
                                        exchange = it.envelope.exchange,
                                        headers = it.props.headers ?: mapOf(),

                                        appId = it.props.appId,
                                        contentEncoding = it.props.contentEncoding,
                                        contentType = it.props.contentType,
                                        correlationId = it.props.correlationId,
                                        deliveryTag = it.envelope.deliveryTag,
                                        expiration = it.props.expiration,
                                        messageId = it.props.messageId,
                                        redelivered = it.envelope.isRedeliver,
                                        replyTo = it.props.replyTo,
                                        timestamp = it.props.timestamp,
                                        type = it.props.type,
                                        userId = it.props.userId,
                                        className = it.props.className,
                                        clusterId = it.props.clusterId,
                                    ).apply {

                                        LoggerFactory.getLogger("RABBITMQ-Kest").info(
                                            """
                                       
                                       
                                        ${toString()}
                                    """.trimIndent()
                                        )

                                        basicAck(it.envelope.deliveryTag, true)
                                    }
                                } ?: throw AssertionFailedError("No message to read")
                        } catch (e: Throwable) {
                            basicNack(it.envelope.deliveryTag, true, true)
                            throw e
                        } finally {
                            if (deleteQueue) queueDelete(queueName)
                        }
                    }
            }

    }
}
