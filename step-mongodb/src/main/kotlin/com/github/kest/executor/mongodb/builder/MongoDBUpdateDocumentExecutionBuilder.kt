package com.github.kest.executor.mongodb.builder

import com.github.kest.executor.mongodb.executor.MongoDBUpdateDocumentExecution
import com.github.kest.executor.mongodb.model.mongoDBProperty
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution

class MongoDBUpdateDocumentExecutionBuilder: ExecutionBuilder<Unit>() {

    lateinit var collection: String
    lateinit var filter: List<Pair<String, Any?>>
    lateinit var update: List<Pair<String, Any?>>

    var host = mongoDBProperty { host }
    var port = mongoDBProperty { port }
    var database = mongoDBProperty { database }
    var user = mongoDBProperty { user }
    var password = mongoDBProperty { password }
    var authSource = mongoDBProperty { authSource }


    override fun build(): Execution<Unit> {
        return MongoDBUpdateDocumentExecution(collection, filter, update, host, port, database, user, password, authSource)
    }
}