package com.github.kest.executor.mongodb.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.StepName
import com.mongodb.client.MongoClients
import org.bson.Document

data class MongoDBUpdateDocumentExecution(
    override val name: StepName?,
    val collection: String,
    val filter: List<Pair<String, Any?>>,
    val update: List<Pair<String, Any?>>,
    val host: String,
    val port: Int,
    val database: String,
    val user: String?,
    val password: String?,
    val authSource: String?,
) : Execution<Unit>() {

    override fun execute() {

        val auth = user?.let { "$user:$password@" } ?: ""
        val authSource = user?.let { "authSource=$authSource" } ?: ""

        MongoClients.create("mongodb://$auth$host/?$authSource").getDatabase(database)
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
