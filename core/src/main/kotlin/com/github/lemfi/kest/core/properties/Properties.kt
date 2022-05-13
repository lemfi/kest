@file:Suppress("unused")

package com.github.lemfi.kest.core.properties

import com.github.lemfi.kest.core.logger.LoggerFactory
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigResult
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

val kestconfig: MutableMap<Class<*>, Any> = mutableMapOf()

inline fun <reified E : Any, R> property(l: E.() -> R): R {
    if (kestconfig[E::class.java] == null) {
        listOf(
            System.getProperty("kest-conf", ""),
            System.getenv().getOrDefault("KEST_CONF", ""),
            "/kest.yml"
        )
            .filterNot { it.isNullOrBlank() }
            .first()
            .let { source ->
                val configuration: ConfigResult<E> = ConfigLoader().loadConfig(source)
                runCatching { configuration.getUnsafe() }.getOrElse {
                    ConfigFailureException(source, configuration.getInvalidUnsafe().description())
                }
            }
            .apply { kestconfig[E::class.java] = this }
    }
    return kestconfig[E::class.java]
        .let { if (it is ConfigFailureException) throw it else it as E }
        .l()
}

inline fun <reified E : Any, R> property(
    @Suppress("unused", "unused_parameter")
    inferType: KClass<E>,
    l: E.() -> R
): R = property(l)

data class AutoConfiguration(val autoconfigure: String)

fun autoconfigure() {
    try {
        property<AutoConfiguration, String> { autoconfigure }
    } catch (e: Throwable) {
        LoggerFactory.getLogger("AUTOCONFIGURATION-Kest").debug("no auto configuration set")
        null
    }?.let { cls ->
        try {
            Class.forName(cls).kotlin.createInstance()
        } catch (e: Throwable) {
            LoggerFactory.getLogger("AUTOCONFIGURATION-Kest").error("cannot instantiate configuration", e)
            throw e
        }
    }

}

class ConfigFailureException(source: String, message: String) :
    Throwable("\nFail to load config source $source: \n$message")