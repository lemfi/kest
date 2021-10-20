package com.github.lemfi.kest.json.cli

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.json.model.JsonMap

/**
 * Transform a ByteArray to a JsonMap object
 */
@Suppress("unused")
fun toJson(b: ByteArray): JsonMap {
    return jacksonObjectMapper().readValue(b, JsonMap::class.java)
}