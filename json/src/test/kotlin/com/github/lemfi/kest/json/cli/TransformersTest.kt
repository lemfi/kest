package com.github.lemfi.kest.json.cli

import com.github.lemfi.kest.json.model.JsonArray
import com.github.lemfi.kest.json.model.JsonMap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TransformersTest {

    @Test
    fun ByteArrayToJsonMap() {
        Assertions.assertEquals(
            JsonMap().apply {
                put("hello", "world")
            },
            """
            {
                "hello": "world"
            }
        """.trimIndent().toByteArray().toJsonMap()
        )
    }

    @Test
    fun ByteArrayToJsonArray() {
        Assertions.assertEquals(
            JsonArray().apply {
                add(
                    JsonMap().apply {
                        put("hello", "world")
                    }
                )
            },
            """
            [{
                "hello": "world"
            }]
        """.trimIndent().toByteArray().toJsonArray()
        )
    }
}