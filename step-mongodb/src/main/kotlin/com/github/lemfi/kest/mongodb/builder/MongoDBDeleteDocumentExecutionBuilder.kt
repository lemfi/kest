package com.github.lemfi.kest.mongodb.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.mongodb.executor.MongoDBDeleteDocumentExecution
import com.github.lemfi.kest.mongodb.model.mongoDBProperty

class MongoDBDeleteDocumentExecutionBuilder : ExecutionBuilder<Long> {

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var collection: String

    @Suppress("MemberVisibilityCanBePrivate")
    var filter: Map<String, Any?> = mapOf()

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = mongoDBProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var database = mongoDBProperty { database }


    override fun toExecution(): Execution<Long> {
        return MongoDBDeleteDocumentExecution(
            collection,
            filter,
            connection,
            database,
        )
    }
}