@file:Suppress("unused")

package com.github.lemfi.kest.mariadb.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.mariadb.executor.MariaDBCleanDatabaseExecution
import com.github.lemfi.kest.mariadb.model.mariadbDBProperty

class MariaDBCleanDatabaseExecutionBuilder : ExecutionBuilder<Unit> {

    @Suppress("MemberVisibilityCanBePrivate")
    var connection = mariadbDBProperty { connection }

    @Suppress("MemberVisibilityCanBePrivate")
    var database = mariadbDBProperty { database }

    @Suppress("MemberVisibilityCanBePrivate")
    var user = mariadbDBProperty { user }

    @Suppress("MemberVisibilityCanBePrivate")
    var password = mariadbDBProperty { password }

    private val tables: MutableList<String> = mutableListOf()
    private val except: MutableList<String> = mutableListOf()

    fun tables(vararg table: String) {
        tables.addAll(table)
    }
    fun except(vararg collection: String) {
        except.addAll(collection)
    }

    override fun toExecution(): Execution<Unit> {
        return MariaDBCleanDatabaseExecution(
            connection = connection,
            user = user,
            password = password,
            database = database,
            tables = tables,
            except = except,
        )
    }
}