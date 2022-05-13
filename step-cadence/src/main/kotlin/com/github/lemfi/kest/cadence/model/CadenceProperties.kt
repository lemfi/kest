package com.github.lemfi.kest.cadence.model

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.properties.property

internal data class CadenceProperties(
    val cadence: CadenceProp
)

internal data class CadenceProp(
    val host: String = "localhost",
    val port: Int = 7933
)

internal inline fun <R> cadenceProperty(crossinline l: CadenceProp.() -> R): R {
    val shortcut: CadenceProperties.() -> R = { cadence.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("CADENCE-Kest").debug("No configuration found for cadence")
        CadenceProp().l()
    }
}