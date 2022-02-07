package com.github.lemfi.kest.mongodb.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.mongodb.executor.MongoDBInsertDocumentExecution
import com.github.lemfi.kest.mongodb.model.mongoDBProperty

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