package com.github.lemfi.kest.mongodb.model

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.properties.property

internal data class MongoDBProperties(
    val mongodb: MongoDBProp
)

internal data class MongoDBProp(
    val connection: String = "mongodb://localhost:27017",
    val database: String = "test",
)

internal inline fun <R> mongoDBProperty(crossinline l: MongoDBProp.() -> R): R {
    val shortcut: MongoDBProperties.() -> R = { mongodb.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("MONGODB-Kest").debug("No configuration found for mongodb")
        MongoDBProp().l()
    }
}