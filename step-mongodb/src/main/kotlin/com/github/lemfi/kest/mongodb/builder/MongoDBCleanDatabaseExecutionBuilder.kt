@file:Suppress("unused")

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

    private val collections: MutableList<String> = mutableListOf()
    private val except: MutableList<String> = mutableListOf()
    fun collections(vararg collection: String) {
        collections.addAll(collection)
    }
    fun except(vararg collection: String) {
        except.addAll(collection)
    }


    override fun toExecution(): Execution<Unit> {
        return MongoDBCleanDatabaseExecution(
            connection = connection,
            database = database,
            collections = collections,
            except = except,
        )
    }
}