package com.github.lemfi.kest.gherkin.junit5

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.properties.property

internal data class GherkinProperties(
    val gherkin: GherkinProp
)

internal data class GherkinProp(
    val stepDefinitions: List<String> = emptyList(),
)

internal inline fun <R> gherkinProperty(crossinline l: GherkinProp.() -> R): R {
    val shortcut: GherkinProperties.() -> R = { gherkin.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("GHERKIN-Kest").debug("No configuration found for http, use default values")
        GherkinProp().l()
    }
}