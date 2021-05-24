package com.github.lemfi.kest.executor.http.builder

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.executor.http.executor.HttpExecution
import com.github.lemfi.kest.executor.http.model.HttpResponse

class HttpCallExecutionBuilder<T>(val cls: Class<T>) : ExecutionBuilder<HttpResponse<T>>() {

    private var name: StepName? = null
    fun name(l: ()->String) {
        name = StepName(l())
    }

    lateinit var url: String
    var method: String = "GET"
    var body: Any? = null
    var contentType: String? = null
    var expectedContentType: String? = null
    val headers = mutableMapOf<String, String>()
    var followRedirect = false


    override fun build(): Execution<HttpResponse<T>> {
        return HttpExecution(name, url, method, cls, body, headers, contentType, expectedContentType, followRedirect)
    }
}