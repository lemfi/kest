package com.github.kest.executor.mongodb.cli

import com.github.kest.executor.mongodb.builder.MongoDBInsertDocumentExecutionBuilder
import com.github.kest.executor.mongodb.builder.MongoDBUpdateDocumentExecutionBuilder
import com.github.kest.executor.mongodb.model.mongoDBProperty
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution
import com.mongodb.client.MongoClients
import org.bson.Document

inline fun ScenarioBuilder.`insert mongo document`(
    name: String? = null,
    retryStep: RetryStep? = null,
    crossinline h: MongoDBInsertDocumentExecutionBuilder.() -> Unit
) {
    Step(
        name = name?.let { StepName(it) } ?: StepName("insert mongo document"),
        scenarioName = this.name!!,
        execution = { MongoDBInsertDocumentExecutionBuilder().apply(h).build() },
        retry = retryStep
    )
        .apply { steps.add(this) }
}

inline fun ScenarioBuilder.`update mongo document`(
    name: String? = null,
    retryStep: RetryStep? = null,
    crossinline h: MongoDBUpdateDocumentExecutionBuilder.() -> Unit
) {
    Step(
        name = name?.let { StepName(it) } ?: StepName("update mongo document"),
        scenarioName = this.name!!,
        execution = { MongoDBUpdateDocumentExecutionBuilder().apply(h).build() },
        retry = retryStep
    )
        .apply { steps.add(this) }
}

fun `insert mongo document`(collection: String, data: String) {

    MongoClients.create(mongoDBProperty { connection })
        .getDatabase(mongoDBProperty { database })
        .getCollection(collection)
        .insertOne(Document.parse(data))
}

fun `clean mongo database`() {

    MongoClients.create(mongoDBProperty { connection })
        .getDatabase(mongoDBProperty { database })
        .let { database ->
            database.listCollectionNames().forEach {
                database.getCollection(it).deleteMany(Document.parse("{}"))
            }
        }
}