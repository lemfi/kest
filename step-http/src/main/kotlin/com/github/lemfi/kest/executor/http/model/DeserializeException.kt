package com.github.lemfi.kest.executor.http.model

class DeserializeException(cls: Class<*>, val data: String, e: Throwable) : Throwable("Could not deserialize $data to $cls", e)