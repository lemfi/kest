package com.github.lemfi.kest.mongodb.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.mongodb.executor.MongoDBCleanDatabaseExecution
import com.github.lemfi.kest.mongodb.model.mongoDBProperty

class MongoDBCleanDatabaseExecutionBuilder : ExecutionBuilder<Unit> {

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = mongoDBProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var database = mongoDBProperty { database }


    override fun toExecution(): Execution<Unit> {
        return MongoDBCleanDatabaseExecution(
            connection,
            database,
        )
    }
}