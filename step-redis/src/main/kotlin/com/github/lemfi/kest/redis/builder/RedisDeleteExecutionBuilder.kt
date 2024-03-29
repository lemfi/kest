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

    @Deprecated("use deleteKey instead", replaceWith = ReplaceWith("deleteKey (key)"))
    fun `delete key`(key: () -> String) = deleteKey(key)
    fun deleteKey(key: () -> String) = RedisDelete(key = key()).apply { delete = this }

    @Deprecated("use fromNamespace instead", replaceWith = ReplaceWith("this fromNamespace namespace"))
    infix fun RedisDelete.`from namespace`(namespace: String) = fromNamespace(namespace)
    infix fun RedisDelete.fromNamespace(namespace: String) = also { it.namespace = namespace }

    override fun toExecution(): Execution<Unit> {
        return RedisRemoveKeyExecution(host, port, db, delete)
    }
}

data class RedisDelete(
    var namespace: String? = null,
    var key: String,
)