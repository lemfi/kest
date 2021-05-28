package com.github.lemfi.kest.executor.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.ExecutionDescription
import com.github.lemfi.kest.executor.rabbitmq.executor.RabbitMQQueueCreationExecution
import com.github.lemfi.kest.executor.rabbitmq.model.rabbitMQProperty

class RabbitMQQueueCreationExecutionBuilder : ExecutionBuilder<Unit> {

    private var description: ExecutionDescription? = null
    fun description(l: ()->String) {
        description = ExecutionDescription(l())
    }

    lateinit var queue: String

    /** exchange / routing key */
    lateinit var bind: Pair<String, String>

    var protocol = rabbitMQProperty { protocol }
    var host = rabbitMQProperty { host }
    var port = rabbitMQProperty { port }
    var vhost = rabbitMQProperty { vhost }
    var user = rabbitMQProperty { user }
    var password = rabbitMQProperty { password }

    override fun toExecution(): Execution<Unit> {
        return RabbitMQQueueCreationExecution(
            description, queue, bind, protocol, host, port, vhost, user, password,
        )
    }
}