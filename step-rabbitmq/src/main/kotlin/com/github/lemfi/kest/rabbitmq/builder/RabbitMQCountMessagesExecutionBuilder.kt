@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.rabbitmq.executor.RabbitMQCountMessagesExecution
import com.github.lemfi.kest.rabbitmq.model.RabbitMQMessageCount
import com.github.lemfi.kest.rabbitmq.model.rabbitMQProperty

class RabbitMQCountMessagesExecutionBuilder : ExecutionBuilder<RabbitMQMessageCount> {

    lateinit var queue: String

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = rabbitMQProperty { managementapi.connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var user = rabbitMQProperty { managementapi.user }

    @Suppress("MemberVisibilityCanBePrivate")
    var password = rabbitMQProperty { managementapi.password }

    @Suppress("MemberVisibilityCanBePrivate")
    var vhost = rabbitMQProperty { vhost }

    override fun toExecution(): Execution<RabbitMQMessageCount> {
        return RabbitMQCountMessagesExecution(
            connection = connection,
            vhost = vhost,
            queue = queue,
            user = user,
            password = password,
        )
    }
}