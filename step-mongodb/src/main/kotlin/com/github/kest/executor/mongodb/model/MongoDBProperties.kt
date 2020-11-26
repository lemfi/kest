package com.github.kest.executor.mongodb.model

import com.github.lemfi.kest.core.properties.property
import org.slf4j.LoggerFactory

data class MongoDBProperties(
        val mongodb: MongoDBProp
)

data class MongoDBProp(
        val host: String,
        val port: Int,
        val user: String? = null,
        val password: String? = null,
        val database: String = "test",
        val authSource: String = ""
)

inline fun <R> mongoDBProperty(crossinline l: MongoDBProp.()->R): R {
    val shortcut: MongoDBProperties.()->R = { mongodb.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("MONGODB-Kest").error("No configuration found for mongodb", e)
        throw e
    }
}