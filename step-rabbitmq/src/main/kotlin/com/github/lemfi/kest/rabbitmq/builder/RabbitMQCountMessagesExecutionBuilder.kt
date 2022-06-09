@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.executor.RabbitMQCountMessagesExecution
import com.github.lemfi.kest.rabbitmq.model.rabbitMQProperty

class RabbitMQCountMessagesExecutionBuilder : ExecutionBuilder<Long> {

    lateinit var queue: String

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = rabbitMQProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var vhost = rabbitMQProperty { vhost }

    override fun toExecution(): Execution<Long> {
        return RabbitMQCountMessagesExecution(
            connection = connection,
            vhost = vhost,
            queue = queue,
        )
    }
}