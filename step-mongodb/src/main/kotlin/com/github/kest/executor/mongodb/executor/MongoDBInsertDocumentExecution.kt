package com.github.kest.executor.mongodb.executor

import com.github.lemfi.kest.core.model.Execution
import com.mongodb.client.MongoClients
import org.bson.Document

data class MongoDBInsertDocumentExecution(
    val document: String,
    val collection: String,
    val connection: String,
    val database: String,
) : Execution<Unit>() {

    override fun execute() {

        MongoClients.create(connection).getDatabase(database)
            .getCollection(collection)
            .insertOne(Document.parse(document))

    }
}
