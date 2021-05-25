package com.github.lemfi.kest.executor.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.ExecutionDescription
import com.github.lemfi.kest.executor.rabbitmq.executor.RabbitMQMessageExecution
import com.github.lemfi.kest.executor.rabbitmq.model.rabbitMQProperty

class RabbitMQMessageExecutionBuilder : ExecutionBuilder<Unit>() {

    private var description: ExecutionDescription? = null
    fun description(l: ()->String) {
        description = ExecutionDescription(l())
    }

    lateinit var message: String
    lateinit var routingKey: String

    var protocol = rabbitMQProperty { protocol }
    var host = rabbitMQProperty { host }
    var port = rabbitMQProperty { port }
    var vhost = rabbitMQProperty { vhost }
    var user = rabbitMQProperty { user }
    var password = rabbitMQProperty { password }
    var exchange = rabbitMQProperty { exchange }
    var timeout = rabbitMQProperty { timeout }


    override fun build(): Execution<Unit> {
        return RabbitMQMessageExecution(
            description,
            message,
            protocol,
            host,
            port,
            vhost,
            user,
            password,
            exchange,
            routingKey,
            timeout,
            rabbitMQProperty { consumedMessageListener },
        )
    }
}