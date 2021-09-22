package com.github.lemfi.kest.executor.http.model

import com.github.lemfi.kest.core.properties.property
import org.slf4j.LoggerFactory

data class HTTPProperties(
    val http: HttpProp
)

data class HttpProp(
    val timeout: Long = 0L,
)

inline fun <R> httpProperty(crossinline l: HttpProp.() -> R): R {
    val shortcut: HTTPProperties.() -> R = { http.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("HTTP-Kest").debug("No configuration found for http, use default values")
        HttpProp().l()
    }
}