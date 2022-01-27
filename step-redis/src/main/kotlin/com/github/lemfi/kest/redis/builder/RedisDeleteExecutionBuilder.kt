@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.redis.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.redis.executor.RedisRemoveKeyExecution
import com.github.lemfi.kest.redis.model.redisProperty

class RedisDeleteExecutionBuilder : ExecutionBuilder<Unit> {

    var host = redisProperty { host }
    var port = redisProperty { port }
    var db = redisProperty { db }

    private lateinit var delete: RedisDelete

    fun `delete key`(key: () -> String) = RedisDelete(key = key()).apply { delete = this }
    infix fun RedisDelete.`from namespace`(namespace: String) = also { it.namespace = namespace }

    override fun toExecution(): Execution<Unit> {
        return RedisRemoveKeyExecution(host, port, db, delete)
    }
}

data class RedisDelete(
    var namespace: String? = null,
    var key: String,
)