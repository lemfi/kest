@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.redis.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.redis.executor.RedisInsertKeyExecution
import com.github.lemfi.kest.redis.model.redisProperty

class RedisInsertExecutionBuilder : ExecutionBuilder<Unit> {

    var host = redisProperty { host }
    var port = redisProperty { port }
    var db = redisProperty { db }

    private lateinit var insert: RedisInsert

    fun insert(data: () -> String) = RedisInsert(data = data()).apply { insert = this }

    @Deprecated("use withKey instead", replaceWith = ReplaceWith("this withKey key"))
    infix fun RedisInsert.`with key`(key: String) = withKey(key)
    infix fun RedisInsert.withKey(key: String) = also { it.key = key }

    @Deprecated("use inNamespace instead", replaceWith = ReplaceWith("this inNamespace namespace"))
    infix fun RedisInsert.`in namespace`(namespace: String) = inNamespace(namespace)
    infix fun RedisInsert.inNamespace(namespace: String) = also { it.namespace = namespace }

    override fun toExecution(): Execution<Unit> {
        return RedisInsertKeyExecution(
            host,
            port,
            db,
            insert.apply { requireNotNull(key) { "no key specified for insertion" } })
    }
}

data class RedisInsert(
    var namespace: String? = null,
    var key: String? = null,
    var data: String,
)