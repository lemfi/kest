package com.github.lemfi.kest.mongodb.executor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import com.mongodb.client.MongoClients
import org.bson.Document

private val jsonwriter = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

internal data class MongoDBDeleteDocumentExecution(
    val collection: String,
    val filter: Map<String, Any?>,
    val connection: String,
    val database: String,
) : Execution<Long>() {

    override fun execute(): Long {

        return MongoClients
            .create(connection)
            .run {
                getDatabase(database)
                    .getCollection(collection)
                    .deleteMany(
                        Document().apply {
                            filter.forEach { put(it.key, it.value) }
                        },
                    )
                    .deletedCount
                    .also { result ->

                        close()

                        LoggerFactory.getLogger("MONGODB-Kest").info(
                            """
                                |
                                |Delete documents: 
                                |
                                |-----------------------------------------------------------------------------
                                |database: $database
                                |collection: $collection
                                |filter: 
                                |${jsonwriter.writeValueAsString(filter)}
                                |
                                |-----------------------------------------------------------------------------
                                |result: 
                                |
                                |$result document${if (result > 0) "s" else ""} deleted
                            """.trimMargin()
                        )
                    }
            }
    }
}
