package com.github.lemfi.kest.mariadb.executor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.json.cli.toJsonString
import com.github.lemfi.kest.json.model.JsonMap
import java.sql.DriverManager

private val jsonwriter = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

internal data class MariaDBReadExecution(
    val connection: String,
    val user: String,
    val password: String,
    val database: String,
    val sql: String,
) : Execution<List<JsonMap>>() {


    override fun execute(): List<JsonMap> {

        val lines = mutableListOf<JsonMap>()
        DriverManager.getConnection(connection, user, password).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(this.sql).use { rs ->
                    while (rs.next()) {
                        val data = JsonMap()
                        lines.add(data)
                        var i = 1
                        do {
                            var isOk = true
                            runCatching {
                                val column = rs.metaData.getColumnLabel(i)
                                val cls = rs.metaData.getColumnClassName(i)
                                data.put(column, rs.getObject(i, Class.forName(cls))) }
                                .onFailure { isOk = false }
                                .onSuccess { i++ }
                        } while (isOk)
                    }
                }
            }
        }


        LoggerFactory.getLogger("MARIADB-Kest").info(
            """
            |READ database: $database
            |
            |$sql
            |
            |${lines.joinToString("\n") { jsonwriter.writeValueAsString(it) }}
        """.trimMargin()
        )

        return lines
    }
}
