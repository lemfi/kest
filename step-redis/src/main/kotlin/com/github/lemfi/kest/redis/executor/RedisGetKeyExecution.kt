package com.github.lemfi.kest.redis.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.redis.builder.RedisRead
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis

class RedisGetKeyExecution(
    private val host: String,
    private val port: Int,
    private val db: Int,
    private val read: RedisRead,
) : Execution<String?>() {

    override fun execute(): String? {

        val key = if (read.namespace != null) "${read.namespace}:${read.key}" else read.key

        LoggerFactory.getLogger("REDIS-Kest").info("Get Key $key")

        return with(
            Jedis(host, port).apply {
                select(this@RedisGetKeyExecution.db)
            })
        {
            get(key).apply {

                LoggerFactory.getLogger("REDIS-Kest").info(this)
            }
        }

    }
}