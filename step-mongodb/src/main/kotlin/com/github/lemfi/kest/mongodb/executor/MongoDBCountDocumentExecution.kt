package com.github.lemfi.kest.mongodb.executor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import com.mongodb.client.MongoClients
import org.bson.Document

internal data class MongoDBCountDocumentExecution(
    val collection: String,
    val filter: Map<String, Any?>,
    val connection: String,
    val database: String,
) : Execution<Long>() {

    private val jsonwriter = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

    override fun execute(): Long {

        return MongoClients
            .create(connection)
            .run {
                getDatabase(database)
                    .getCollection(collection)
                    .countDocuments(
                        Document().apply {
                            filter.forEach { put(it.key, it.value) }
                        },
                    ).also { result ->

                        close()

                        LoggerFactory.getLogger("MONGODB-Kest").info(
                            """
                                |
                                |Count documents: 
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
                                |$result
                            """.trimMargin()
                        )
                    }
            }
    }
}
