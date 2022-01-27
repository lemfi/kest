package com.github.lemfi.kest.executor.rabbitmq.model

data class RabbitMQPublicationProperties(
    val contentType: String?,
    val contentEncoding: String?,
    val deliveryMode: Int?,
    val priority: Int?,
    val correlationId: String?,
    val replyTo: String?,
)