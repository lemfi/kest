package com.github.lemfi.kest.rabbitmq.model

data class RabbitMQMessageCount(
    val ready: Long,
    val unacked: Long,
    val total: Long,
)
