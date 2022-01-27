package com.github.lemfi.kest.redis.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.redis.builder.RedisInsert
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis

class RedisInsertKeyExecution(
    private val host: String,
    private val port: Int,
    private val db: Int,
    private val insert: RedisInsert,
) : Execution<Unit>() {

    override fun execute() {

        val key = if (insert.namespace != null) "${insert.namespace}:${insert.key}" else insert.key

        LoggerFactory.getLogger("REDIS-Kest").info("Insert data on Key $key")

        with(
            Jedis(host, port).apply {
                select(this@RedisInsertKeyExecution.db)
            })
        {
            set(key, insert.data)
        }

    }
}