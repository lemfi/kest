package com.github.lemfi.kest.cadence.model

import com.github.lemfi.kest.core.properties.property
import org.slf4j.LoggerFactory

data class CadenceProperties(
    val cadence: CadenceProp
)

data class CadenceProp(
    val host: String = "localhost",
    val port: Int = 7933
)

inline fun <R> cadenceProperty(crossinline l: CadenceProp.() -> R): R {
    val shortcut: CadenceProperties.() -> R = { cadence.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        LoggerFactory.getLogger("CADENCE-Kest").warn("No configuration found for cadence")
        CadenceProp().l()
    }
}