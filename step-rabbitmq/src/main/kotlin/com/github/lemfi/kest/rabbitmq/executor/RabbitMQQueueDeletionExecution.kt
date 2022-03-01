package com.github.lemfi.kest.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.rabbitmq.client.ConnectionFactory
import org.slf4j.LoggerFactory
import java.net.URLEncoder

internal class RabbitMQQueueDeletionExecution(
    private val queue: String,
    private val connection: String,
    private val vhost: String,
) : Execution<Unit>() {

    private val encodedVhost: String = URLEncoder.encode(vhost, Charsets.UTF_8)

    override fun execute() {

        LoggerFactory.getLogger("RABBITMQ-Kest")
            .info(
                """
                |Queue deletion:
                |
                |vhost: $vhost
                |name: $queue
                """.trimMargin()
            )

        ConnectionFactory().also {
            it.setUri("$connection/$encodedVhost")
        }
            .newConnection("kest connection")
            .createChannel()
            .apply {
                queueDelete(queue)
            }
    }
}
