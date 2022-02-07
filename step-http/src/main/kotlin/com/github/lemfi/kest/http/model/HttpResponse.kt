package com.github.lemfi.kest.http.model

data class HttpResponse<T>(
    val body: T,
    val status: Int,
    val headers: Map<String, List<String>>
)