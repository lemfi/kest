package com.github.lemfi.kest.executor.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.executor.rabbitmq.builder.QueueAndBinding
import com.rabbitmq.client.ConnectionFactory
import org.slf4j.LoggerFactory
import java.net.URLEncoder

class RabbitMQQueueCreationExecution(
    val queueAndBinding: QueueAndBinding,
    val connection: String,
    val vhost: String,
) : Execution<Unit>() {

    val encodedVhost = URLEncoder.encode(vhost, Charsets.UTF_8)

    override fun execute() {

        LoggerFactory.getLogger("RABBITMQ-Kest")
            .info(
                """
                |Queue creation:
                |
                |vhost: $vhost
                |name: ${queueAndBinding.queue} 
                |
                |${
                    if (queueAndBinding.exchange != null) {
                        """bound to exchange: 
                |          exchange: ${queueAndBinding.exchange} 
                |          routing key: ${queueAndBinding.routingKey}""".trimMargin()
                    } else {
                        """bound to default ("") exchange"""
                    }
                }""".trimMargin()
            )

        ConnectionFactory().also {
            it.setUri("$connection/$encodedVhost")
        }
            .newConnection("kest connection")
            .createChannel()
            .apply {
                queueDeclare(queueAndBinding.queue, false, false, true, mutableMapOf())
                if (queueAndBinding.exchange != null && queueAndBinding.routingKey != null)
                    queueBind(queueAndBinding.queue, queueAndBinding.exchange, queueAndBinding.routingKey)
            }
    }
}
