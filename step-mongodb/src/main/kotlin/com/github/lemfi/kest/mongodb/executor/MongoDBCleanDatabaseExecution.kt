package com.github.lemfi.kest.mongodb.executor

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import com.mongodb.client.MongoClients
import org.bson.Document

internal data class MongoDBCleanDatabaseExecution(
    val connection: String,
    val database: String,
) : Execution<Unit>() {

    override fun execute() {

        LoggerFactory.getLogger("MONGODB-Kest").info(
            """
            |Clean database: $database
        """.trimMargin()
        )

        MongoClients.create(connection)
            .let {client ->
                client
                    .getDatabase(database)
                    .let { database ->
                        database.listCollectionNames().forEach { collection ->
                            LoggerFactory.getLogger("MONGODB-Kest").info(
                                """
                        |Clean collection: $collection
                    """.trimMargin()
                            )
                            database.getCollection(collection).deleteMany(Document.parse("{}"))
                                .let {
                                    LoggerFactory.getLogger("MONGODB-Kest").info(
                                        """
                                |Deleted: ${it.deletedCount} documents
                            """.trimMargin()
                                    )
                                }
                        }
                    }
                    .apply { client.close() }
            }
    }
}