package com.github.lemfi.kest.redis.model


import com.github.lemfi.kest.core.properties.property
import org.slf4j.LoggerFactory

internal data class RedisProperties(
    val redis: RedisProp
)

internal data class RedisProp(
    val host: String = "localhost",
    val port: Int = 6379,
    val db: Int = 0,
)

internal fun <R> redisProperty(l: RedisProp.() -> R): R {
    val shortcut: RedisProperties.() -> R = { redis.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("REDIS-Kest").debug("No configuration found for redis, use default values")
        RedisProp().l()
    }
}