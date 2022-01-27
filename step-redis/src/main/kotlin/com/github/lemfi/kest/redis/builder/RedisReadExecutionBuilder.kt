@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.redis.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.redis.executor.RedisGetKeyExecution
import com.github.lemfi.kest.redis.model.redisProperty

class RedisReadExecutionBuilder : ExecutionBuilder<String?> {

    var host = redisProperty { host }
    var port = redisProperty { port }
    var db = redisProperty { db }

    private lateinit var read: RedisRead

    fun `read key`(key: () -> String) = RedisRead(key = key()).apply { read = this }
    infix fun RedisRead.`from namespace`(namespace: String) = also { it.namespace = namespace }

    override fun toExecution(): Execution<String?> {
        return RedisGetKeyExecution(host, port, db, read)
    }
}

data class RedisRead(
    var namespace: String? = null,
    var key: String,
)