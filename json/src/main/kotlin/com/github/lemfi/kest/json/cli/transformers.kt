package com.github.lemfi.kest.json.cli

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.json.model.JsonArray
import com.github.lemfi.kest.json.model.JsonMap

/**
 * Transform a ByteArray to a JsonMap object
 */
@Suppress("unused")
@Deprecated("use toJsonMap instead", replaceWith = ReplaceWith("b.toJsonMap()"))
fun toJson(b: ByteArray): JsonMap {
    return jacksonObjectMapper().readValue(b, object : TypeReference<JsonMap>() {})
}

/**
 * Transform a ByteArray to a JsonMap object
 */
@Suppress("unused")
fun ByteArray.toJsonMap(): JsonMap {
    return jacksonObjectMapper().readValue(this, object : TypeReference<JsonMap>() {})
}

/**
 * Transform a ByteArray to a JsonMap object
 */
@Suppress("unused")
fun ByteArray.toJsonArray(): JsonArray {
    return jacksonObjectMapper().readValue(this, object : TypeReference<JsonArray>() {})
}
