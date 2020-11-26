package com.github.lemfi.kest.executor.http.executor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.executor.http.model.HttpResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.slf4j.LoggerFactory

data class HttpExecution<T>(
        val url: String,
        val method: String,
        val returnType: Class<T>,
        val body: Any? = null,
        val headers: MutableMap<String, String>,
        override val withResult: HttpResponse<T>.()->Unit = {},
        val contentType: String = "application/json; charset=utf-8"
): Execution<HttpResponse<T>>() {

    companion object {
        private val mappers = mutableMapOf<String, ResponseBody?.(cls: Class<*>) -> Any?>()
                .apply {
                    put("application/json; charset=utf-8") {
                        jacksonObjectMapper().readValue(this?.string(), it)
                    }
                    put("application/json") {
                        jacksonObjectMapper().readValue(this?.string(), it)
                    }
                    put("plain/text") {
                        this?.string()?.trim()
                    }
                }

        fun addMapper(contentType: String, mapper: ResponseBody?.(cls: Class<*>) -> Any) {
            mappers.put(contentType, mapper)
        }

        fun getMapper(contentType: String) = (mappers.get(contentType) ?: throw IllegalArgumentException("""no mapper found for content type "$contentType", please register one by calling `HttpExecution.addMapper($contentType) { ... }"""))
    }

    @Suppress("unchecked_cast")
    override fun execute(): HttpResponse<T> {

        LoggerFactory.getLogger("HTTP-kest").info(
                """ |
                    | $method $url
                    | $headers
                    | $body
                """.trimMargin()
        )

        return OkHttpClient.Builder().build().newCall(
                Request.Builder()
                        .url(url)
                        .apply {
                            headers.put("Content-Type", contentType)
                            headers.forEach { addHeader(it.key, it.value) }
                        }
                        .method(method, body?.toString()?.toRequestBody(contentType.toMediaTypeOrNull()))
                        .build()
        ).execute().let {
            HttpResponse(
                    (it.header("Content-Type") ?: "application/json").let { contentType ->
                        (getMapper(contentType).invoke(it.body, returnType) as T
                                ?: throw IllegalArgumentException("""no mapper found for content type "$contentType", please register one by calling `HttpExecution.addMapper($contentType) { ... }"""))
                    },
                    it.code,
                    it.headers
                            .let { headers -> headers.map { it.first } }.toSet()
                            .map { key -> key to it.headers(key) }
                            .toMap()
            )
        }
    }
}
