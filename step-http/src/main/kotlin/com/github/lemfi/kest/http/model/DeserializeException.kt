package com.github.lemfi.kest.http.model

internal class DeserializeException(cls: Class<*>, val data: String, e: Throwable) :
    Throwable("Could not deserialize $data to $cls", e)