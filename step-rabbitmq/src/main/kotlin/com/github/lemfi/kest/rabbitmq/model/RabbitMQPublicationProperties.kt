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
) {
    override fun toString(): String {
        return listOfNotNull(
            messageId?.let { "messageId: $messageId" },
            replyTo?.let { "replyTo: $replyTo" },
            correlationId?.let { "correlationId: $correlationId" },
            deliveryMode?.let { "deliveryMode: $deliveryMode" },
            type?.let { "type: $type" },
            contentType?.let { "contentType: $contentType" },
            contentEncoding?.let { "contentEncoding: $contentEncoding" },
            expiration?.let { "expiration: $expiration" },
            timestamp?.let { "timestamp: $timestamp" },
            userId?.let { "userId: $userId" },
            appId?.let { "appId: $appId" }
        )
            .joinToString("\n    ")
    }
}