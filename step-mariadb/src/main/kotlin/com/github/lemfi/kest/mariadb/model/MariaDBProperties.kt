package com.github.lemfi.kest.mariadb.model

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.properties.property

internal data class MariaDBProperties(
    val mariadb: MariaDBProp
)

internal data class MariaDBProp(
    val connection: String = "jdbc:mariadb://localhost:3306",
    val database: String = "test",
    val user: String = "root",
    val password: String = "test",
)

internal inline fun <R> mariadbDBProperty(crossinline l: MariaDBProp.() -> R): R {
    val shortcut: MariaDBProperties.() -> R = { mariadb.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("MARIADB-Kest").debug("No configuration found for mariadb")
        MariaDBProp().l()
    }
}