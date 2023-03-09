package com.github.lemfi.kest.http.builder

import com.fasterxml.jackson.core.type.TypeReference
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.http.executor.HttpExecution
import com.github.lemfi.kest.http.model.HttpResponse
import com.github.lemfi.kest.http.model.httpProperty

class HttpCallExecutionBuilder<T>(private val typeReference: TypeReference<T>) :

    ExecutionBuilder<HttpResponse<T>> {

    companion object {
        inline operator fun <reified T : Any> invoke(): HttpCallExecutionBuilder<T> =
            HttpCallExecutionBuilder(object : TypeReference<T>() {})
    }

    lateinit var url: String
    var method: String = "GET"
    var body: Any? = null
    var contentType: String? = null
    val headers = mutableMapOf<String, String>()
    var followRedirect = false

    @Suppress("MemberVisibilityCanBePrivate")
    var logResponseBody = true

    @Suppress("MemberVisibilityCanBePrivate")
    var timeout: Long? = httpProperty { timeout }

    override fun toExecution(): Execution<HttpResponse<T>> {
        @Suppress("MemberVisibilityCanBePrivate")
        return HttpExecution(
            url = url,
            method = method,
            returnType = typeReference,
            body = body,
            headers = headers,
            contentType = contentType,
            followRedirect = followRedirect,
            logBody = logResponseBody,
            timeout = timeout,
        )
    }
}