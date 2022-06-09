package com.github.lemfi.kest.rabbitmq.executor

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import com.rabbitmq.client.ConnectionFactory
import java.net.URLEncoder


internal data class RabbitMQCountMessagesExecution(
    val connection: String,
    val vhost: String,
    val queue: String,
) : Execution<Long>() {

    private val encodedVhost: String = URLEncoder.encode(vhost, Charsets.UTF_8)

    override fun execute(): Long {
        return ConnectionFactory().also {

            it.setUri("$connection/$encodedVhost")
        }
            .newConnection("kest connection")
            .createChannel()
            .messageCount(queue)
            .also { count ->

                LoggerFactory.getLogger("RABBITMQ-Kest").info(
                    """
                    |Count messages in queue:
                    |
                    |vhost: $vhost
                    |queue: $queue
                    |
                    |count: $count
                    |
                    |""".trimMargin()
                )
            }


    }

}
