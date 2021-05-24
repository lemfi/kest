package com.github.kest.executor.mongodb.cli

import com.github.kest.executor.mongodb.builder.MongoDBInsertDocumentExecutionBuilder
import com.github.kest.executor.mongodb.builder.MongoDBUpdateDocumentExecutionBuilder
import com.github.kest.executor.mongodb.model.mongoDBProperty
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepPostExecution
import com.mongodb.client.MongoClients
import org.bson.Document

inline fun ScenarioBuilder.`insert mongo document`(
    retryStep: RetryStep? = null,
    crossinline h: MongoDBInsertDocumentExecutionBuilder.() -> Unit
) {
    Step(name!!, { MongoDBInsertDocumentExecutionBuilder().apply(h).build() }, retry = retryStep).apply {
        steps.add(this)
    }
}

inline fun ScenarioBuilder.`update mongo document`(
    retryStep: RetryStep? = null,
    crossinline h: MongoDBUpdateDocumentExecutionBuilder.() -> Unit
) {
    Step(name!!, { MongoDBUpdateDocumentExecutionBuilder().apply(h).build() }, retry = retryStep).apply {
        steps.add(this)
    }
}

fun `insert mongo document`(collection: String, data: String) {
    val auth = mongoDBProperty { user }?.let { "${mongoDBProperty { user }}:${mongoDBProperty { password }}@" } ?: ""
    val authSource = mongoDBProperty { user }?.let { "authSource=${mongoDBProperty { authSource }}" } ?: ""

    MongoClients.create("mongodb://$auth${mongoDBProperty { host }}/?$authSource")
        .getDatabase(mongoDBProperty { database })
        .getCollection(collection)
        .insertOne(Document.parse(data))
}

fun `clean mongo database`() {
    val auth = mongoDBProperty { user }?.let { "${mongoDBProperty { user }}:${mongoDBProperty { password }}@" } ?: ""
    val authSource = mongoDBProperty { user }?.let { "authSource=${mongoDBProperty { authSource }}" } ?: ""

    MongoClients.create("mongodb://$auth${mongoDBProperty { host }}/?$authSource")
        .getDatabase(mongoDBProperty { database })
        .let { database ->
            database.listCollectionNames().forEach {
                database.getCollection(it).deleteMany(Document.parse("{}"))
            }
        }
}