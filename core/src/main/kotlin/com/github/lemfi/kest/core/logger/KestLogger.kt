package com.github.lemfi.kest.core.logger

import org.slf4j.Logger
import java.io.ByteArrayOutputStream
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.concurrent.getOrSet

class LoggerFactory {
    companion object {
        fun getLogger(name: String = "Kest"): Logger {
            return Proxy.newProxyInstance(
                object {}::class.java.classLoader,
                arrayOf(Logger::class.java),
                LoggerInvocationHandler(org.slf4j.LoggerFactory.getLogger(name))
            ) as Logger
        }
    }
}

private val threadLocalLogger: ThreadLocal<ByteArrayOutputStream> = ThreadLocal<ByteArrayOutputStream>()
private fun ThreadLocal<ByteArrayOutputStream>.getOrDefault() = getOrSet { ByteArrayOutputStream() }

private class LoggerInvocationHandler(private val logger: Logger) : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method, args: Array<Any?>?): Any {
        return method
            .apply {
                val condition = when (method.name) {
                    "trace" -> logger.isTraceEnabled
                    "debug" -> logger.isDebugEnabled
                    "info" -> logger.isInfoEnabled
                    "warn" -> logger.isWarnEnabled
                    "error" -> logger.isErrorEnabled
                    else -> true
                }
                if (condition) {

                    if (args?.size == 1 && args[0] is String) {
                        threadLocalLogger
                            .getOrDefault()
                            .apply {
                                (args[0] as String).toByteArray().also {
                                    write(it)
                                    flush()
                                }
                            }
                    }

                    if (args?.size == 2 && args[0] is String && args[1] is Throwable) {
                        threadLocalLogger
                            .getOrDefault()
                            .apply {
                                ((args[0] as String) to args[1] as Throwable)
                                    .let { (message, throwable) ->
                                        write(message.toByteArray())
                                        write(throwable.stackTraceToString().toByteArray())
                                        flush()
                                    }

                            }
                    }

                    if (args != null) {
                        method.invoke(logger, *args)
                    } else {
                        method.invoke(logger)
                    }
                }
            }
    }
}

@Suppress("unused")
internal interface KestLogs {
    companion object {
        @JvmStatic
        fun getLog() = threadLocalLogger.getOrDefault().toString()

        @JvmStatic
        fun resetLog() = threadLocalLogger.getOrDefault().reset()
    }
}