package com.github.lemfi.kest.samplerabbit

import com.rabbitmq.client.*

var connection: Connection? = null
var channel: Channel? = null
var consumer: String? = null

fun startRabbitApplication(port: String = "5672") {
    val connectionFactory = ConnectionFactory().also { it.setUri("amqp://guest:guest@localhost:$port/%2F") }
    connection = connectionFactory.newConnection("rabbit application")
    channel = connection!!.createChannel()
    channel!!.queueDeclare("R2D2", true, true, false, emptyMap())
    consumer = channel!!.basicConsume("R2D2", object : DefaultConsumer(channel) {
        override fun handleDelivery(
            consumerTag: String,
            envelope: Envelope,
            properties: AMQP.BasicProperties,
            body: ByteArray
        ) {
            val deliveryTag = envelope.deliveryTag

            Thread.sleep(8000)
            channel.basicPublish(
                "",
                body.toString(Charsets.UTF_8),
                AMQP.BasicProperties(),
                "Au secours ${body.toString(Charsets.UTF_8)}, vous êtes notre seul espoir !".toByteArray(Charsets.UTF_8)
            )

            channel.basicAck(deliveryTag, false)
        }
    })
}


fun stopRabbitApplication() {
    channel?.close()
    connection?.close()
}