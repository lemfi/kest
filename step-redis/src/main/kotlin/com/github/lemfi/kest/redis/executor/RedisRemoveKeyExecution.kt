package com.github.lemfi.kest.redis.executor

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.redis.builder.RedisDelete
import redis.clients.jedis.Jedis

class RedisRemoveKeyExecution(
    private val host: String,
    private val port: Int,
    private val db: Int,
    private val delete: RedisDelete,
) : Execution<Unit>() {

    override fun execute() {

        val key = if (delete.namespace != null) "${delete.namespace}:${delete.key}" else delete.key

        LoggerFactory.getLogger("REDIS-Kest").info("Remove Key $key")

        with(
            Jedis(host, port).apply {
                select(this@RedisRemoveKeyExecution.db)
            })
        {
            keys(key).onEach {
                del(it)
            }
        }

    }
}