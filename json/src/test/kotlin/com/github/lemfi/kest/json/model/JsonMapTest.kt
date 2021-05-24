package com.github.lemfi.kest.json.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JsonMapTest {

    @Test
    fun `navigation in json is operational`() {

        val json = JsonMap().apply {
            put("key", "value")
            put("key2", "value2")
            put("key3", JsonMap().apply {
                put("key31", "value31")
                put("key32", "value32")
                put("key33", listOf(true, false))
                put("key34", listOf(listOf("a", "b"), listOf("c", "d")))
            })
            put("array", listOf(JsonMap().apply {
                put("elem11", "val11")
                put("elem12", "val12")
                put("elem13", listOf(1, 2, 3, 4))
            }, JsonMap().apply {
                put("elem21", "val21")
                put("elem22", "val22")
                put("elem23", listOf(5, 6, 7, 8))
            }))

        }

        Assertions.assertEquals("value", json.getForPath("key"))
        Assertions.assertEquals("value2", json.getForPath("key2"))
        Assertions.assertEquals("value31", json.getForPath("key3", "key31"))
        Assertions.assertEquals("value32", json.getForPath("key3", "key32"))
        Assertions.assertEquals(true, json.getForPath("key3", "key33[0]"))
        Assertions.assertEquals("a", json.getForPath("key3", "key34[0][0]"))
        Assertions.assertEquals("b", json.getForPath("key3", "key34[0][1]"))
        Assertions.assertEquals("c", json.getForPath("key3", "key34[1][0]"))
        Assertions.assertEquals("d", json.getForPath("key3", "key34[1][1]"))

        Assertions.assertEquals("val11", json.getForPath("array[0]", "elem11"))
        Assertions.assertEquals("val12", json.getForPath("array[0]", "elem12"))
        Assertions.assertEquals(1, json.getForPath("array[0]", "elem13[0]"))
        Assertions.assertEquals(2, json.getForPath("array[0]", "elem13[1]"))
        Assertions.assertEquals(3, json.getForPath("array[0]", "elem13[2]"))
        Assertions.assertEquals(4, json.getForPath("array[0]", "elem13[3]"))

        Assertions.assertEquals("val21", json.getForPath("array[1]", "elem21"))
        Assertions.assertEquals("val22", json.getForPath("array[1]", "elem22"))
        Assertions.assertEquals(5, json.getForPath("array[1]", "elem23[0]"))
        Assertions.assertEquals(6, json.getForPath("array[1]", "elem23[1]"))
        Assertions.assertEquals(7, json.getForPath("array[1]", "elem23[2]"))
        Assertions.assertEquals(8, json.getForPath("array[1]", "elem23[3]"))
    }
}