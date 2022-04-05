package com.github.lemfi.kest.json.model

import com.github.lemfi.kest.core.properties.property
import org.slf4j.LoggerFactory

internal data class JsonProperties(
    val json: JsonProp
)

data class JsonProp(
    val checkArraysOrder: Boolean = true,
    val ignoreUnknownProperties: Boolean = false,
)

internal fun <R> jsonProperty(l: JsonProp.() -> R): R {
    val shortcut: JsonProperties.() -> R = { json.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("RABBITMQ-Kest").debug("No configuration found for rabbitmq, use default values")
        JsonProp().l()
    }
}