package com.github.lemfi.kest.mariadb.executor

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import java.sql.DriverManager

internal data class MariaDBUpdateOperationExecution(
    val connection: String,
    val user: String,
    val password: String,
    val database: String,
    val sql: String,
) : Execution<Int>() {


    override fun execute(): Int {

        val updatedLines = DriverManager.getConnection(connection, user, password).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate(this.sql)
            }
        }


        LoggerFactory.getLogger("MARIADB-Kest").info(
            """
            |UPDATE OPERATION database: $database
            |
            |$sql
            |
            |${updatedLines} affected lines
        """.trimMargin()
        )

        return updatedLines
    }
}
