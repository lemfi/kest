package com.github.lemfi.kest.executor.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.StepName
import com.rabbitmq.client.ConnectionFactory
import java.net.URLEncoder

class RabbitMQQueueCreationExecution(
    override val name: StepName?,
    val queueName: String,
    val bind: Pair<String, String>,
    val protocol: String,
    val host: String,
    val port: Int,
    vhost: String,
    val user: String,
    val password: String,
) : Execution<Unit>() {

    val encodedVhost = URLEncoder.encode(vhost, Charsets.UTF_8)

    override fun execute() {

        ConnectionFactory().also {
            it.setUri("$protocol://$user:$password@$host:$port/$encodedVhost")
        }
            .newConnection("kest connection")
            .createChannel()
            .apply {
                queueDeclare(queueName, false, false, true, mutableMapOf())
                queueBind(queueName, bind.first, bind.second)
            }
    }
}
