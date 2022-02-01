package com.github.lemfi.kest.executor.rabbitmq.model

import java.util.*

data class RabbitMQMessage<T>(
    val message: T,
    val headers: Map<String, Any>,
    val exchange: String,
    val routingKey: String,
    val deliveryTag: Long,
    val redelivered: Boolean,

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
    val className: String?,
    val clusterId: String?,
)