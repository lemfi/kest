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

    @Deprecated("use readKey instead", replaceWith = ReplaceWith("readKey (key)"))
    fun `read key`(key: () -> String) = readKey(key)
    fun readKey(key: () -> String) = RedisRead(key = key()).apply { read = this }

    @Deprecated("use fromNamespace instead", replaceWith = ReplaceWith("this fromNamespace namespace"))
    infix fun RedisRead.`from namespace`(namespace: String) = fromNamespace(namespace)
    infix fun RedisRead.fromNamespace(namespace: String) = also { it.namespace = namespace }

    override fun toExecution(): Execution<String?> {
        return RedisGetKeyExecution(host, port, db, read)
    }
}

data class RedisRead(
    var namespace: String? = null,
    var key: String,
)