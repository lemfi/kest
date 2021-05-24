package com.github.lemfi.kest.executor.rabbitmq.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.executor.rabbitmq.listener.startRabbitMQProxy
import com.github.lemfi.kest.executor.rabbitmq.model.RabbitMQSnifferProp
import com.rabbitmq.client.*
import org.opentest4j.AssertionFailedError
import java.net.URLEncoder
import java.util.*


data class RabbitMQMessageExecution(
    override val name: StepName?,
    val message: String,
    val protocol: String,
    val host: String,
    val port: Int,
    val vhost: String,
    val user: String,
    val password: String,
    val exchange: String,
    val routingKey: String,
    val timeout: Long,
    val rabbitMQSnifferProp: RabbitMQSnifferProp,
) : Execution<Unit>() {

    private var messageConsumed = false

    val encodedVhost = URLEncoder.encode(vhost, Charsets.UTF_8)
    override fun execute() {

        if (rabbitMQSnifferProp.active && rabbitMQSnifferProp.startedByKest) startRabbitMQProxy(
            user,
            password,
            host,
            port,
            encodedVhost,
            rabbitMQSnifferProp
        )

        val listeningQueue = "kest-${UUID.randomUUID()}"

        ConnectionFactory().also {

            it.setUri("$protocol://$user:$password@$host:$port/$encodedVhost")
        }
            .newConnection("kest connection")
            .createChannel().apply {
                if (rabbitMQSnifferProp.active) queueDeclare(listeningQueue, false, false, true, mutableMapOf())
            }
            .publish(listeningQueue)
            .wait(listeningQueue)

    }

    private fun Channel.publish(replyTo: String): Channel {

        basicPublish(
            exchange, routingKey, AMQP.BasicProperties().builder()
                .replyTo(replyTo)
                .build(), message.toByteArray(Charsets.UTF_8)
        )

        return this
    }

    private fun Channel.wait(listeningQueue: String) {
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

            var waiting = timeout
            while (!messageConsumed && waiting > 0) {
                waiting -= 100
                Thread.sleep(100)
            }
            if (waiting <= 0) throw AssertionFailedError("Message not consumed in time")
        } else {
            Thread.sleep(timeout)
        }


    }
}
