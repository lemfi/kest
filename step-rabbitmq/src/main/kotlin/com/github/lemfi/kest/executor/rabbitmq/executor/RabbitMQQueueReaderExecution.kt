package com.github.lemfi.kest.executor.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.ExecutionDescription
import com.github.lemfi.kest.core.model.StepName
import com.rabbitmq.client.ConnectionFactory
import org.opentest4j.AssertionFailedError
import java.net.URLEncoder

class RabbitMQQueueReaderExecution<T>(
    override val description: ExecutionDescription?,
    val queueName: String,
    val deleteQueue: Boolean,
    val protocol: String,
    val host: String,
    val port: Int,
    vhost: String,
    val user: String,
    val password: String,
    val l: ByteArray.() -> T,
) : Execution<T>() {

    val encodedVhost = URLEncoder.encode(vhost, Charsets.UTF_8)

    override fun execute(): T {

        return ConnectionFactory().also {

            it.setUri("$protocol://$user:$password@$host:$port/$encodedVhost")
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
