package com.github.lemfi.kest.rabbitmq.model

import java.util.Date

data class RabbitMQPublicationProperties(
    val deliveryMode: Int?,
    val type: String?,
    val contentType: String?,
    val contentEncoding: String?,
    val messageId: String?,
    val correlationId: String?,
    val replyTo: String?,
    val expiration: String?,
    val timestamp: Date?,
    val userId: String?,
    val appId: String?,
)