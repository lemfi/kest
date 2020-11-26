package com.github.lemfi.kest.executor.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.executor.rabbitmq.executor.RabbitMQQueueReaderExecution
import com.github.lemfi.kest.executor.rabbitmq.model.rabbitMQProperty

class RabbitMQQueueReaderExecutionBuilder<T>: ExecutionBuilder<T>() {

    lateinit var queueName: String
    lateinit var messageTransformer: ByteArray.()->T

    var protocol = rabbitMQProperty { protocol }
    var host = rabbitMQProperty { host }
    var port = rabbitMQProperty { port }
    var vhost = rabbitMQProperty { vhost }
    var user = rabbitMQProperty { user }
    var password = rabbitMQProperty { password }

    override fun build(): Execution<T> {
        return RabbitMQQueueReaderExecution(
                queueName, protocol, host, port, vhost, user, password, messageTransformer
        )
    }
}