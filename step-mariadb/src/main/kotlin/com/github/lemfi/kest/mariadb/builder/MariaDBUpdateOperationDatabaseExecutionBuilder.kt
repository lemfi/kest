@file:Suppress("unused")

package com.github.lemfi.kest.mariadb.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.mariadb.executor.MariaDBUpdateOperationExecution
import com.github.lemfi.kest.mariadb.model.mariadbDBProperty

class MariaDBUpdateOperationDatabaseExecutionBuilder : ExecutionBuilder<Int> {

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = mariadbDBProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var database = mariadbDBProperty { database }

    @Suppress("MemberVisibilityCanBePrivate")
    var user = mariadbDBProperty { user }

    @Suppress("MemberVisibilityCanBePrivate")
    var password = mariadbDBProperty { password }

    var sql: String? = null

    override fun toExecution(): Execution<Int> {
        return MariaDBUpdateOperationExecution(
            connection = connection,
            user = user,
            password = password,
            database = database,
            sql = sql ?: error("no sql provided!"),
        )
    }
}