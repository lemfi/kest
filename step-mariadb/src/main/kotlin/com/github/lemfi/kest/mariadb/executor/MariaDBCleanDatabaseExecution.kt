package com.github.lemfi.kest.mariadb.executor

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import java.sql.DriverManager


internal data class MariaDBCleanDatabaseExecution(
    val connection: String,
    val user: String,
    val password: String,
    val database: String,
    val tables: List<String>,
    val except: List<String>,
) : Execution<Unit>() {

    override fun execute() {

        LoggerFactory.getLogger("MARIADB-Kest").info(
            """
            |Clean database: $database
        """.trimMargin()
        )

        DriverManager.getConnection(connection, user, password).use { conn ->
            val tables = mutableListOf<String>()
            conn.createStatement().use { stmt ->
                stmt.executeQuery("SHOW TABLES").use { rs ->
                    while (rs.next()) {
                        tables.add(rs.getString(1)) //result is "Hello World!"
                    }
                }
            }
            conn.createStatement().use { stmt ->
                stmt.executeQuery("SET FOREIGN_KEY_CHECKS = 0")
            }
            while (tables.isNotEmpty()) {
                tables.shuffle()
                val tablesIter = tables.iterator()
                while (tablesIter.hasNext()) {
                    val table = tablesIter.next()
                    runCatching {
                        conn.createStatement().use { stmt ->
                            stmt.executeQuery("TRUNCATE TABLE $table")
                        }
                    }
                        .onSuccess {
                        LoggerFactory.getLogger("MARIADB-Kest").info(
                            """
                            |Clean table: $table
                        """.trimMargin()
                        )
                        tablesIter.remove()
                    }
                }
            }
            conn.createStatement().use { stmt ->
                stmt.executeQuery("SET FOREIGN_KEY_CHECKS = 1")
            }
        }
    }
}