package com.github.kest.executor.mongodb.cli

import com.mongodb.client.MongoClients
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.Step
import com.github.kest.executor.mongodb.builder.MongoDBInsertDocumentExecutionBuilder
import com.github.kest.executor.mongodb.model.mongoDBProperty
import org.bson.Document

inline fun ScenarioBuilder.`insert mongo document`(crossinline h: MongoDBInsertDocumentExecutionBuilder.()->Unit): Step<Unit> {
    return Step(MongoDBInsertDocumentExecutionBuilder().apply(h).build()).apply {
        steps.add(this)
    }
}

fun `clean mongo database`() {
    val auth = mongoDBProperty { user }?.let { "${mongoDBProperty { user }}:${mongoDBProperty { password }}@" } ?: ""
    val authSource = mongoDBProperty { user }?.let { "authSource=${mongoDBProperty { authSource }}" } ?: ""

    MongoClients.create("mongodb://$auth${mongoDBProperty { host }}/?$authSource").getDatabase(mongoDBProperty { database })
            .let {
                database ->
                database.listCollectionNames().forEach {

                    database.getCollection(it).deleteMany(Document.parse("{}"))

                }

            }
}