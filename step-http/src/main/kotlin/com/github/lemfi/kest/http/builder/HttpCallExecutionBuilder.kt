package com.github.lemfi.kest.http.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.http.executor.HttpExecution
import com.github.lemfi.kest.http.model.HttpResponse
import com.github.lemfi.kest.http.model.httpProperty

class HttpCallExecutionBuilder<T>(private val cls: Class<T>) : ExecutionBuilder<HttpResponse<T>> {

    lateinit var url: String
    var method: String = "GET"
    var body: Any? = null
    var contentType: String? = null
    val headers = mutableMapOf<String, String>()
    var followRedirect = false

    @Suppress("MemberVisibilityCanBePrivate")
    var timeout: Long? = httpProperty { timeout }

    override fun toExecution(): Execution<HttpResponse<T>> {
        return HttpExecution(url, method, cls, body, headers, contentType, followRedirect, timeout)
    }
}