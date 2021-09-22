package com.github.kest.executor.mongodb.executor

import com.github.lemfi.kest.core.model.Execution
import com.mongodb.client.MongoClients
import org.bson.Document
import org.slf4j.LoggerFactory

internal data class MongoDBUpdateDocumentExecution(
    val collection: String,
    val filter: List<Pair<String, Any?>>,
    val update: List<Pair<String, Any?>>,
    val connection: String,
    val database: String,
) : Execution<Unit>() {

    override fun execute() {

        LoggerFactory.getLogger("MONGODB-Kest").info(
            """
            |Updateocument: 
            |
            |database: $database
            |collection: $collection
            |filter: 
            |${filter.map { "${it.first}: ${it.second}\n" }}
            |
            |update: 
            |${update.map { "${it.first}: ${it.second}\n" }}
        """.trimMargin()
        )

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
