@file:Suppress("unused")

package com.github.lemfi.kest.http.executor

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.http.model.DeserializeException
import com.github.lemfi.kest.http.model.FileDataPart
import com.github.lemfi.kest.http.model.FilePart
import com.github.lemfi.kest.http.model.HttpResponse
import com.github.lemfi.kest.http.model.MultipartBody
import com.github.lemfi.kest.http.model.ParameterPart
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

object KestHttp {

    /**
     * Register a content type decoder for HTTP calls
     * @param contentType Content-Type for decoder
     * @param transformer function to decode InputStream with given Content-Type into an Object
     */
    fun registerContentTypeDecoder(contentType: String, transformer: InputStream?.(cls: TypeReference<*>) -> Any?) =
        HttpExecution.addMapper(contentType) { cls ->
            this?.run {
                ByteArrayInputStream(readAllBytes()).run {
                    use { inputStream ->
                        inputStream.transformer(cls) to inputStream.let {
                            try {
                                it.reset()
                                it.readAllBytes().toString(Charsets.UTF_8).trim()
                            } catch (e: Throwable) {
                                ""
                            }
                        }
                    }
                }
            } ?: (null to "null")
        }
}

internal data class HttpExecution<T>(
    val url: String,
    val method: String,
    val returnType: TypeReference<T>,
    val body: Any? = null,
    val headers: MutableMap<String, String>,
    val contentType: String?,
    val followRedirect: Boolean,
    val timeout: Long?,
) : Execution<HttpResponse<T>>() {

    private val accept = headers.getOrDefault("Accept", null)

    companion object {
        private val mappers = mutableMapOf<String, InputStream?.(cls: TypeReference<*>) -> Pair<Any?, String?>>()
            .apply {
                put("application/json") {
                    this?.readAllBytes()?.toString(Charsets.UTF_8)?.trim()?.let { data ->
                        try {
                            jacksonObjectMapper().readValue(data, it)
                        } catch (e: Throwable) {
                            throw DeserializeException(it.type.javaClass, data, e)
                        } to data
                    } ?: (null to null)
                }
                put("text") {
                    readText()
                }
            }

        private fun InputStream?.readText() = this?.readAllBytes()?.toString(Charsets.UTF_8)?.trim()?.let { data ->
            data to data
        } ?: (null to null)

        fun addMapper(contentType: String, mapper: InputStream?.(cls: TypeReference<*>) -> Pair<Any?, String?>) {
            mappers[contentType] = mapper
        }

        fun getMapper(contentType: String) = mappers[contentType.substringBefore(";").trim()]
            ?: mappers[contentType.substringBefore(";").trim().substringBefore("/")]
            ?: throw IllegalArgumentException("""no decoder found for content type "$contentType", please register one by calling `KestHttp.registerContentTypeDecoder("$contentType") { ... }""")
    }

    @Suppress("unchecked_cast")
    override fun execute(): HttpResponse<T> {

        LoggerFactory.getLogger("HTTP-kest").info(
            """ | Request
            | 
            | $method $url
            | $headers
            | ${body.toLog()}
            | 
            | """.trimMargin()
        )

        return if (body is MultipartBody) {
            OkHttpClient.Builder()
                .followRedirects(followRedirect)
                .readTimeout(timeout ?: 0, TimeUnit.MILLISECONDS)
                .build().newCall(
                    Request.Builder()
                        .url(url)
                        .apply {
                            headers.forEach { addHeader(it.key, it.value) }
                        }
                        .method(method, okhttp3.MultipartBody.Builder()
                            .apply {
                                body.parts.forEach {
                                    when (it) {
                                        is FilePart -> addFormDataPart(
                                            it.name,
                                            it.filename,
                                            it.file.asRequestBody(it.contentType?.toMediaTypeOrNull())
                                        )
                                        is ParameterPart -> addFormDataPart(it.name, it.value)
                                        is FileDataPart -> addFormDataPart(
                                            it.name,
                                            it.filename,
                                            it.data.toRequestBody(it.contentType?.toMediaTypeOrNull())
                                        )
                                    }
                                }
                            }
                            .setType("multipart/form-data".toMediaType())
                            .build())
                        .build()
                )
                .execute()
                .toHttpResponse()

        } else {
            OkHttpClient.Builder()
                .followRedirects(followRedirect)
                .readTimeout(timeout ?: 0, TimeUnit.MILLISECONDS)
                .build().newCall(
                    Request.Builder()
                        .url(url)
                        .apply {
                            contentType?.also { headers["Content-Type"] = it }
                            headers.forEach { addHeader(it.key, it.value) }
                        }
                        .method(method, body
                            ?.let { body ->
                                when (body) {
                                    is String -> body.toRequestBody(contentType?.toMediaTypeOrNull())
                                    is ByteArray -> body.toRequestBody(contentType?.toMediaTypeOrNull())
                                    is File -> body.asRequestBody(contentType?.toMediaTypeOrNull())
                                    else -> throw IllegalArgumentException("only String, ByteArray or File is accepted for HTTP request body")
                                }

                            })
                        .build()
                ).execute().toHttpResponse()
        }
    }

    @Suppress("unchecked_cast")
    private fun Response.toHttpResponse(): HttpResponse<T> {
        var content: String? = null
        return try {
            (header("Content-Type") ?: accept ?: "text/plain")
                .let { contentType ->
                    (getMapper(contentType).invoke(body?.byteStream(), returnType))
                }.let { body ->
                    HttpResponse(
                        body = body.first as T,
                        status = code,
                        headers = headers
                            .let { headers -> headers.map { it.first } }
                            .toSet()
                            .associateWith { key -> headers(key) }
                    ).also { content = body.second }
                }
        } catch (e: DeserializeException) {
            content = e.data
            throw e
        } finally {
            LoggerFactory.getLogger("HTTP-kest").info(
                """ | Response
                | $code
                | ${headers.joinToString("\n") { "${it.first}: ${it.second}" }}
                | 
                | $content
                |
                |""".trimMargin()
            )
        }
    }

    private fun Any?.toLog() = this?.let { data ->
        when (data) {
            is ByteArray -> "ByteArray(${data.size})"
            is File -> "File(${data.path}"
            is String -> data
            else -> data::class.simpleName
        }
    } ?: ""
}
