package com.github.kest.executor.mongodb.builder

import com.github.kest.executor.mongodb.executor.MongoDBUpdateDocumentExecution
import com.github.kest.executor.mongodb.model.mongoDBProperty
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.ExecutionDescription

class MongoDBUpdateDocumentExecutionBuilder : ExecutionBuilder<Unit>() {

    private var description: ExecutionDescription? = null
    fun description(l: ()->String) {
        description = ExecutionDescription(l())
    }

    lateinit var collection: String
    lateinit var filter: List<Pair<String, Any?>>
    lateinit var update: List<Pair<String, Any?>>

    var connection = mongoDBProperty { connection }
    var database = mongoDBProperty { database }


    override fun build(): Execution<Unit> {
        return MongoDBUpdateDocumentExecution(
            description,
            collection,
            filter,
            update,
            connection,
            database,
        )
    }
}