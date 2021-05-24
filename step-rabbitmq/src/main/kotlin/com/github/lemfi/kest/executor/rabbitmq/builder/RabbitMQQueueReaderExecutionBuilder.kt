package com.github.lemfi.kest.executor.rabbitmq.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.executor.rabbitmq.executor.RabbitMQQueueReaderExecution
import com.github.lemfi.kest.executor.rabbitmq.model.rabbitMQProperty

class RabbitMQQueueReaderExecutionBuilder<T> : ExecutionBuilder<T>() {

    private var name: StepName? = null
    fun name(l: ()->String) {
        name = StepName(l())
    }

    lateinit var queue: String
    lateinit var messageTransformer: ByteArray.() -> T
    var deleteQueue = false

    var protocol = rabbitMQProperty { protocol }
    var host = rabbitMQProperty { host }
    var port = rabbitMQProperty { port }
    var vhost = rabbitMQProperty { vhost }
    var user = rabbitMQProperty { user }
    var password = rabbitMQProperty { password }

    override fun build(): Execution<T> {
        return RabbitMQQueueReaderExecution(
            name, queue, deleteQueue, protocol, host, port, vhost, user, password, messageTransformer
        )
    }
}