package com.github.lemfi.kest.executor.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.executor.rabbitmq.executor.RabbitMQQueueCreationExecution
import com.github.lemfi.kest.executor.rabbitmq.model.rabbitMQProperty

class RabbitMQQueueCreationExecutionBuilder: ExecutionBuilder<Unit>() {

    lateinit var name: String
    /** exchange / routing key */
    lateinit var bind: Pair<String, String>

    var protocol = rabbitMQProperty { protocol }
    var host = rabbitMQProperty { host }
    var port = rabbitMQProperty { port }
    var vhost = rabbitMQProperty { vhost }
    var user = rabbitMQProperty { user }
    var password = rabbitMQProperty { password }

    override fun build(): Execution<Unit> {
        return RabbitMQQueueCreationExecution(
                name, bind, protocol, host, port, vhost, user, password,
        )
    }
}