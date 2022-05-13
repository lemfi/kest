package com.github.lemfi.kest.core.properties

import com.github.lemfi.kest.core.logger.LoggerFactory

internal data class AssertionProperties(
    val assertions: AssertionProp
)

data class AssertionProp(
    val filterStackTraces: Boolean = true,
)

internal fun <R> assertionProperty(l: AssertionProp.() -> R): R {
    val shortcut: AssertionProperties.() -> R = { assertions.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("Core-Kest").debug("No configuration found for assertions, use default values")
        AssertionProp().l()
    }
}