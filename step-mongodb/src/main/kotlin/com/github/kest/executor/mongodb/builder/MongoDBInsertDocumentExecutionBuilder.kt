package com.github.kest.executor.mongodb.builder

import com.github.kest.executor.mongodb.executor.MongoDBInsertDocumentExecution
import com.github.kest.executor.mongodb.model.mongoDBProperty
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.StepName

class MongoDBInsertDocumentExecutionBuilder : ExecutionBuilder<Unit>() {

    private var name: StepName? = null
    fun name(l: ()->String) {
        name = StepName(l())
    }
    lateinit var document: String
    lateinit var collection: String

    var host = mongoDBProperty { host }
    var port = mongoDBProperty { port }
    var database = mongoDBProperty { database }
    var user = mongoDBProperty { user }
    var password = mongoDBProperty { password }
    var authSource = mongoDBProperty { authSource }


    override fun build(): Execution<Unit> {
        return MongoDBInsertDocumentExecution(name, document, collection, host, port, database, user, password, authSource)
    }
}