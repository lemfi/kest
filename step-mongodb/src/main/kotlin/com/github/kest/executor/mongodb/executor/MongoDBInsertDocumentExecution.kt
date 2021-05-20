package com.github.kest.executor.mongodb.executor

import com.github.lemfi.kest.core.model.Execution
import com.mongodb.client.MongoClients
import org.bson.Document

data class MongoDBInsertDocumentExecution(
        val document: String,
        val collection: String,
        val host: String,
        val port: Int,
        val database: String,
        val user: String?,
        val password: String?,
        val authSource: String?,
): Execution<Unit>() {

    override fun execute() {

        val auth = user?.let { "$user:$password@" } ?: ""
        val authSource = user?.let { "authSource=$authSource" } ?: ""

        MongoClients.create("mongodb://$auth$host/?$authSource").getDatabase(database)
                .getCollection(collection)
                .insertOne(Document.parse(document))

    }
}
