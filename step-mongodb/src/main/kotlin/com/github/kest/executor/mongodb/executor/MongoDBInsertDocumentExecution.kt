package com.github.kest.executor.mongodb.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.ExecutionDescription
import com.github.lemfi.kest.core.model.StepName
import com.mongodb.client.MongoClients
import org.bson.Document

data class MongoDBInsertDocumentExecution(
    override val description: ExecutionDescription?,
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
