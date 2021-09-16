package com.github.kest.executor.mongodb.builder

import com.github.kest.executor.mongodb.executor.MongoDBCleanDatabaseExecution
import com.github.kest.executor.mongodb.model.mongoDBProperty
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution

class MongoDBCleanDatabaseExecutionBuilder : ExecutionBuilder<Unit> {

    lateinit var collection: String

    var connection = mongoDBProperty { connection }
    var database = mongoDBProperty { database }


    override fun toExecution(): Execution<Unit> {
        return MongoDBCleanDatabaseExecution(
            collection,
            connection,
            database,
        )
    }
}