package com.github.lemfi.kest.executor.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.executor.rabbitmq.builder.QueueAndBinding
import com.rabbitmq.client.ConnectionFactory
import java.net.URLEncoder

class RabbitMQQueueCreationExecution(
    val queueAndBinding: QueueAndBinding,
    val connection: String,
    vhost: String,
) : Execution<Unit>() {

    val encodedVhost = URLEncoder.encode(vhost, Charsets.UTF_8)

    override fun execute() {

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
