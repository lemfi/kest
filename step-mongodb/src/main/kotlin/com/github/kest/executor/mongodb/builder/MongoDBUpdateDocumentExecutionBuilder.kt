package com.github.kest.executor.mongodb.builder

import com.github.kest.executor.mongodb.executor.MongoDBUpdateDocumentExecution
import com.github.kest.executor.mongodb.model.mongoDBProperty
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution

class MongoDBUpdateDocumentExecutionBuilder : ExecutionBuilder<Unit> {

    lateinit var collection: String
    lateinit var filter: List<Pair<String, Any?>>
    lateinit var update: List<Pair<String, Any?>>

    var connection = mongoDBProperty { connection }
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