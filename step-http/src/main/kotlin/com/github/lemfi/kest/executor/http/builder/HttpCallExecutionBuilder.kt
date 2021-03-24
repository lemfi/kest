package com.github.lemfi.kest.executor.http.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.executor.http.executor.HttpExecution
import com.github.lemfi.kest.executor.http.model.HttpResponse

class HttpCallExecutionBuilder<T>(val cls: Class<T>): ExecutionBuilder<HttpResponse<T>>() {

    lateinit var url: String
    var method: String = "GET"
    var body: Any? = null
    var contentType: String? = null
    var expectedContentType: String? = null
    val headers = mutableMapOf<String, String>()
    var followRedirect = false
    private var withResult: HttpResponse<T>.()->Unit = {}

    fun withResult(l: HttpResponse<T>.()->Unit) { withResult = l }

    override fun build(): Execution<HttpResponse<T>> {
        return HttpExecution(url, method, cls, body, headers, withResult, contentType, expectedContentType, followRedirect)
    }
}