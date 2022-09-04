package com.github.lemfi.kest.mongodb.executor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.json.model.getForPath
import com.mongodb.client.MongoClients
import org.bson.Document
import org.bson.types.ObjectId

internal data class MongoDBReadDocumentExecution(
    val collection: String,
    val filter: Map<String, Any?>,
    val connection: String,
    val database: String,
) : Execution<List<JsonMap>>() {

    private val jsonwriter = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

    override fun execute(): List<JsonMap> {

        return MongoClients
            .create(connection)
            .run {
                getDatabase(database)
                    .getCollection(collection)
                    .find(
                        Document().apply {
                            filter.forEach { put(it.key, it.value) }
                        },
                    )
                    .toList()
                    .map {
                        it.toMap() as JsonMap
                    }.map {
                        if (it.containsKey("_id") && it["_id"] is ObjectId) {
                            it["_id"] = it.getForPath<ObjectId>("_id").toHexString()
                        }
                        it
                    }.also { result ->

                        close()

                        LoggerFactory.getLogger("MONGODB-Kest").info(
                            """
                                |
                                |Find documents: 
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
                                |${jsonwriter.writeValueAsString(result)}
                            """.trimMargin()
                        )
                    }
            }
    }
}
