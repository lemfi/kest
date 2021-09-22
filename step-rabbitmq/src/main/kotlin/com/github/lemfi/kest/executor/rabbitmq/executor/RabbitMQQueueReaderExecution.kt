package com.github.lemfi.kest.executor.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.rabbitmq.client.ConnectionFactory
import org.opentest4j.AssertionFailedError
import org.slf4j.LoggerFactory
import java.net.URLEncoder

internal class RabbitMQQueueReaderExecution<T>(
    val queueName: String,
    val deleteQueue: Boolean,
    val connection: String,
    val vhost: String,
    val l: ByteArray.() -> T,
) : Execution<T>() {

    val encodedVhost = URLEncoder.encode(vhost, Charsets.UTF_8)

    override fun execute(): T {

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
                basicGet(queueName, true).let {
                    try {
                        it?.body?.l() ?: throw AssertionFailedError("No message to read")
                    } finally {
                        if (deleteQueue) queueDelete(queueName)
                    }
                }
            }

    }
}
