package com.github.lemfi.kest.mongodb.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.mongodb.executor.MongoDBUpdateDocumentExecution
import com.github.lemfi.kest.mongodb.model.mongoDBProperty

class MongoDBUpdateDocumentExecutionBuilder : ExecutionBuilder<Unit> {

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var collection: String

    @Suppress("MemberVisibilityCanBePrivate")
    var filter: Map<String, Any?> = mapOf()

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var update: Map<String, Any?>

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = mongoDBProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var database = mongoDBProperty { database }


    override fun toExecution(): Execution<Unit> {
        return MongoDBUpdateDocumentExecution(
            collection,
            filter,
            update,
            connection,
            database,
        )
    }
}