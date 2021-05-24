package com.github.lemfi.kest.core.properties

import com.sksamuel.hoplite.ConfigLoader
import org.slf4j.LoggerFactory

val config: MutableMap<Class<*>, Any> = mutableMapOf()

inline fun <reified E : Any, R> property(l: E.() -> R): R {
    val conf =
        when (config.get(E::class.java)) {
            null -> ConfigLoader().loadConfigOrThrow<E>("/${System.getProperty("kest-conf", "kest.yml")}")
                .apply { config[E::class.java] = this }
            else -> config.get(E::class.java) as E
        }
    return conf.l()
}

data class AutoConfiguration(val autoconfigure: String)

fun autoconfigure() {
    try {
        property<AutoConfiguration, String> { autoconfigure }
    } catch (e: Throwable) {
        LoggerFactory.getLogger("AUTOCONFIGURATION-Kest").warn("no auto configuration set")
        null
    }?.let { cls ->
        try {
            Class.forName(cls).getDeclaredConstructor().newInstance()
        } catch (e: Throwable) {
            LoggerFactory.getLogger("AUTOCONFIGURATION-Kest").error("cannot instantiate configuration", e)
            throw e
        }
    }

}