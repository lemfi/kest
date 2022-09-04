package com.github.lemfi.kest.mongodb.executor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import com.mongodb.client.MongoClients
import org.bson.Document

internal data class MongoDBUpdateDocumentExecution(
    val collection: String,
    val filter: Map<String, Any?>,
    val update: Map<String, Any?>,
    val connection: String,
    val database: String,
) : Execution<Unit>() {

    private val jsonwriter = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

    override fun execute() {

        LoggerFactory.getLogger("MONGODB-Kest").info(
            """
            |Update document: 
            |
            |database: $database
            |collection: $collection
            |filter: 
            |${jsonwriter.writeValueAsString(filter)}
            |
            |update: 
            |${jsonwriter.writeValueAsString(update)}
        """.trimMargin()
        )

        MongoClients
            .create(connection)
            .apply {
                getDatabase(database)
                    .getCollection(collection)
                    .updateMany(
                        Document().apply {
                            filter.forEach { put(it.key, it.value) }
                        },
                        Document().apply {
                            put("\$set", Document().apply {
                                update.forEach { put(it.key, it.value) }
                            })
                        },
                    )
                close()
            }
    }
}
