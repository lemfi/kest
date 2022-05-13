package com.github.lemfi.kest.mongodb.executor

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import com.mongodb.client.MongoClients
import org.bson.Document

internal data class MongoDBInsertDocumentExecution(
    val document: String,
    val collection: String,
    val connection: String,
    val database: String,
) : Execution<Unit>() {

    override fun execute() {

        LoggerFactory.getLogger("MONGODB-Kest").info(
            """
            |Insert document: 
            |
            |database: $database
            |collection: $collection
            |
            |document: 
            |$document
        """.trimMargin()
        )

        MongoClients.create(connection).getDatabase(database)
            .getCollection(collection)
            .insertOne(Document.parse(document))

    }
}
