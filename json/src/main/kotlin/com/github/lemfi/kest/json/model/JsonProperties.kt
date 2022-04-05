package com.github.lemfi.kest.json.model

import com.github.lemfi.kest.core.properties.property
import org.slf4j.LoggerFactory

data class JsonProperties(
    val checkArraysOrder: Boolean = true,
    val ignoreUnknownProperties: Boolean = false,
)

internal fun <R> jsonProperty(l: JsonProperties.() -> R): R {
    val shortcut: JsonProperties.() -> R = { l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("JSON-Kest").debug("No configuration found for json, use default values")
        JsonProperties().l()
    }
}