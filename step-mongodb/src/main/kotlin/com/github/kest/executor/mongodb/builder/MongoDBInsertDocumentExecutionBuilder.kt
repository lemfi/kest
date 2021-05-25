package com.github.kest.executor.mongodb.builder

import com.github.kest.executor.mongodb.executor.MongoDBInsertDocumentExecution
import com.github.kest.executor.mongodb.model.mongoDBProperty
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.ExecutionDescription
import com.github.lemfi.kest.core.model.StepName

class MongoDBInsertDocumentExecutionBuilder : ExecutionBuilder<Unit>() {

    private var description: ExecutionDescription? = null
    fun description(l: ()->String) {
        description = ExecutionDescription(l())
    }
    lateinit var document: String
    lateinit var collection: String

    var connection = mongoDBProperty { connection }
    var database = mongoDBProperty { database }

    override fun build(): Execution<Unit> {
        return MongoDBInsertDocumentExecution(description, document, collection, connection, database)
    }
}