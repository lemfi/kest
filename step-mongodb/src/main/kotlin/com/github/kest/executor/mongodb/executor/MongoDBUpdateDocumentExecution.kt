package com.github.kest.executor.mongodb.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.ExecutionDescription
import com.github.lemfi.kest.core.model.StepName
import com.mongodb.client.MongoClients
import org.bson.Document

data class MongoDBUpdateDocumentExecution(
    override val description: ExecutionDescription?,
    val collection: String,
    val filter: List<Pair<String, Any?>>,
    val update: List<Pair<String, Any?>>,
    val connection: String,
    val database: String,
) : Execution<Unit>() {

    override fun execute() {

        MongoClients.create(connection).getDatabase(database)
            .getCollection(collection)
            .updateMany(
                Document().apply {
                    filter.forEach { put(it.first, it.second) }
                },
                Document().apply {
                    put("\$set", Document().apply {
                        update.forEach { put(it.first, it.second) }
                    })
                },
            )
    }
}
