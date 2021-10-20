package com.github.kest.executor.mongodb.builder

import com.github.kest.executor.mongodb.executor.MongoDBInsertDocumentExecution
import com.github.kest.executor.mongodb.model.mongoDBProperty
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution

class MongoDBInsertDocumentExecutionBuilder : ExecutionBuilder<Unit> {

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var document: String

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var collection: String

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = mongoDBProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var database = mongoDBProperty { database }

    override fun toExecution(): Execution<Unit> {
        return MongoDBInsertDocumentExecution(document, collection, connection, database)
    }
}