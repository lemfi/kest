package com.github.lemfi.kest.executor.http.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.ExecutionDescription
import com.github.lemfi.kest.executor.http.executor.HttpExecution
import com.github.lemfi.kest.executor.http.model.HttpResponse

class HttpCallExecutionBuilder<T>(val cls: Class<T>) : ExecutionBuilder<HttpResponse<T>> {

    private var description: ExecutionDescription? = null
    fun description(l: ()->String) {
        description = ExecutionDescription(l())
    }

    lateinit var url: String
    var method: String = "GET"
    var body: Any? = null
    var contentType: String? = null
    val headers = mutableMapOf<String, String>()
    var followRedirect = false


    override fun toExecution(): Execution<HttpResponse<T>> {
        return HttpExecution(description, url, method, cls, body, headers, contentType, followRedirect)
    }
}