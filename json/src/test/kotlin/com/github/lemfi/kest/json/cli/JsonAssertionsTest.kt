package com.github.lemfi.kest.json.cli

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.model.ScenarioName
import com.github.lemfi.kest.json.model.JsonArray
import com.github.lemfi.kest.json.model.JsonMap
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError

private fun assertionBuilder() = AssertionsBuilder(ScenarioName("json test"), null)

class JsonAssertionsTest {

    @Test
    fun `json object with simple types that matches`() {

        assertionBuilder().jsonMatchesObject(
            """
                   {
                        "string": "{{string}}",
                        "number": "{{number}}",
                        "boolean": "{{boolean}}",
                        "a string": "hello",
                        "a number": 1,
                        "a boolean": false
                   } 
                """,
            """
                   {
                        "string": "hello",
                        "number": 1,
                        "boolean": true,
                        "a string": "hello",
                        "a number": 1,
                        "a boolean": false
                   } 
                """
        )
    }

    @Test
    fun `json object with arrays of simple types with errors`() {

        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                   {
                        "strings": "[[{{string}}]]"
                   } 
                """,
                """
                   {
                        "strings": ["hello", 1]
                   } 
                """
            )
        }

        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                   {
                        "numbers": "[[{{number}}]]"
                   } 
                """,
                """
                   {
                        "numbers": [1, "world"],
                   } 
                """
            )
        }

        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                   {
                        "booleans": "[[{{boolean}}]]"
                   } 
                """,
                """
                   {
                        "booleans": [true, "world", 1]
                   } 
                """
            )
        }

        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                   {
                        "some strings": ["hello", "world"]
                   } 
                """,
                """
                   {
                        "some strings": ["hello", "worlds"]
                   } 
                """
            )
        }

        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                   {
                        "some numbers": [1, 2]
                   } 
                """,
                """
                   {
                        "some numbers": [1, true]
                   } 
                """
            )
        }

        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                   {
                        "some booleans": [true, false]
                   } 
                """,
                """
                   {
                        "some booleans": [true, 2]
                   } 
                """
            )
        }
    }

    @Test
    fun `json object with arrays of simple types`() {

        assertionBuilder().jsonMatchesObject(
            """
                   {
                        "strings": "[[{{string}}]]",
                        "numbers": "[[{{number}}]]",
                        "booleans": "[[{{boolean}}]]",
                        "some strings": ["hello", "world"],
                        "some numbers": [1, 2],
                        "some booleans": [true, false]
                   } 
                """,
            """
                   {
                        "strings": ["hello", "world"],
                        "numbers": [1, 2],
                        "booleans": [true, false],
                        "some strings": ["hello", "world"],
                        "some numbers": [1, 2],
                        "some booleans": [true, false]
                   } 
                """
        )
    }

    @Test
    fun `json object with not nullable string fails`() {


        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                   {
                        "string": "{{string}}"
                   } 
                """,
                """
                   {
                        "string": null
                   } 
                """
            )
        }
    }

    @Test
    fun `json int does not match when string requested`() {

        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                   {
                        "string": "{{string}}"
                   } 
                """,
                """
                   {
                        "string": 12
                   } 
                """
            )
        }
    }

    @Test
    fun `json object with nullable string passes`() {

        assertionBuilder().jsonMatchesObject(
            """
                   {
                        "string": "{{string?}}"
                   } 
                """,
            """
                   {
                        "string": null
                   } 
                """
        )
    }

    @Test
    fun `json object with not nullable number fails`() {


        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                   {
                        "number": "{{number}}"
                   } 
                """,
                """
                   {
                        "number": null
                   } 
                """
            )
        }
    }

    @Test
    fun `json object with nullable number passes`() {

        assertionBuilder().jsonMatchesObject(
            """
                   {
                        "number": "{{number?}}"
                   } 
                """,
            """
                   {
                        "number": null
                   } 
                """
        )
    }

    @Test
    fun `json object with not nullable boolean fails`() {


        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                   {
                        "boolean": "{{boolean}}"
                   } 
                """,
                """
                   {
                        "boolean": null
                   } 
                """
            )
        }
    }

    @Test
    fun `json object with nullable boolean passes`() {

        assertionBuilder().jsonMatchesObject(
            """
                   {
                        "boolean": "{{boolean?}}"
                   } 
                """,
            """
                   {
                        "boolean": null
                   } 
                """
        )
    }

    @Test
    fun `json object with number type not matching`() {


        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                   {
                        "number": "{{number}}"
                   } 
                """,
                """
                   {
                        "number": "1"
                   } 
                """
            )
        }
    }

    @Test
    fun `json object with boolean type not matching`() {


        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                        {
                            "boolean": "{{boolean}}"
                        } 
                    """,
                """
                       {
                            "boolean": "true"
                       } 
                    """
            )
        }
    }

    @Test
    fun `json object with value not matching`() {


        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                        {
                            "data": "1234"
                        } 
                    """,
                """
                       {
                            "data": "5678"
                       } 
                    """
            )
        }
    }

    @Test
    fun `json object with key not matching`() {

        assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatchesObject(
                """
                        {
                            "data1": "1234"
                        } 
                    """,
                """
                       {
                            "data2": "1234"
                       } 
                    """
            )
        }
    }

    @Test
    fun `json array of simple types strings`() {

        assertionBuilder().jsonMatchesArray(
            """
                    {
                        "string": "{{string?}}",
                        "number": "{{number?}}",
                        "boolean": "{{boolean?}}",
                        "a string": "hello",
                        "a number": 1,
                        "a boolean": false
                   } 
                """,
            """
                   [
                        {
                            "string": "hello",
                            "number": 1,
                            "boolean": true,
                            "a string": "hello",
                            "a number": 1,
                            "a boolean": false
                       },
                       {
                            "string": null,
                            "number": null,
                            "boolean": null,
                            "a string": "hello",
                            "a number": 1,
                            "a boolean": false
                       }
                   ] 
                """
        )
    }

    @Test
    fun `json array of object in subtype`() {

        JsonMatcher.addMatcher("{{descdata}}", """{"data": "{{number}}"}""")

        assertionBuilder().jsonMatchesObject(
            """
                    {
                        "string": "{{string?}}",
                        "array": "[[{{descdata}}]]"
                   } 
                """,
            """
                        {
                            "string": "hello",
                            "array": [
                                {"data": 1},
                                {"data": 2}
                            ]
                        }
                """
        )
    }

    @Test
    fun `json array of nullable object in subtype`() {

        JsonMatcher.addMatcher("{{descdata}}", """{"data": "{{number}}"}""")

        assertionBuilder().jsonMatchesObject(
            """
                    {
                        "string": "{{string?}}",
                        "array": "[[{{descdata?}}]]"
                   } 
                """,
            """
                        {
                            "string": "hello",
                            "array": [
                                {"data": 1},
                                null
                            ]
                        }
                """
        )
    }

    @Test
    fun `json array of nullable object in subtypez`() {

        JsonMatcher.addMatcher("{{descdata}}", """{"data": "{{number}}"}""")

        assertionBuilder().jsonMatchesObject(
            """
                    {
                        "string": "{{string?}}",
                        "array": "[[{{descdata?}}]]?"
                   } 
                """,
            """
                        {
                            "string": "hello",
                            "array": null
                        }
                """
        )
    }

    @Test
    fun `matcher registration - observed is string`() {

        JsonMatcher.addMatcher("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatchesObject(
            """
                   {{mydata}} 
                """,
            """
                   {
                            "string": "hello",
                            "number": 1,
                            "boolean": false
                       } 
                """
        )
    }

    @Test
    fun `matcher registration - matcher in subtype`() {

        JsonMatcher.addMatcher("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatchesObject(
            """
                   {
                        "data": "{{mydata}}"
                   } 
                """,
            """
                   {
                        "data": {
                            "string": "hello",
                            "number": 1,
                            "boolean": false
                        }
                   } 
                """
        )
    }

    @Test
    fun `matcher registration - nullable matcher in subtype`() {

        JsonMatcher.addMatcher("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatchesObject(
            """
                   {
                        "data": "{{mydata?}}"
                   } 
                """,
            """
                   {
                        "data": null
                   } 
                """
        )
    }

    @Test
    fun `matcher registration - observed is JsonMap`() {

        JsonMatcher.addMatcher("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatchesObject(
            """
                   {{mydata}} 
                """,
            JsonMap().apply {
                put("string", "hello")
                put("number", 1)
                put("boolean", false)
            }
        )
    }

    @Test
    fun `matcher registration - observed an array displayed as string`() {

        JsonMatcher.addMatcher("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatchesArray(
            """
                   {{mydata}}
                """,
            """
                   [{
                            "string": "hello",
                            "number": 1,
                            "boolean": false
                    }] 
                """
        )
    }

    @Test
    fun `matcher registration - matcher in subtype as array`() {

        JsonMatcher.addMatcher("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatchesObject(
            """
                   {
                        "data": "[[{{mydata}}]]"
                   } 
                """,
            """
                   {
                        "data": [{
                            "string": "hello",
                            "number": 1,
                            "boolean": false
                        }]
                   } 
                """
        )
    }

    @Test
    fun `matcher registration - nullable matcher in subtype as array`() {

        JsonMatcher.addMatcher("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatchesObject(
            """
                   {
                        "data": "[[{{mydata?}}]]"
                   } 
                """,
            """
                   {
                        "data": [{
                            "string": "hello",
                            "number": 1,
                            "boolean": false
                        }, null]
                   } 
                """
        )
    }

    @Test
    fun `matcher registration - observed is JsonArray`() {

        JsonMatcher.addMatcher("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatchesArray(
            """
                   {{mydata}} 
                """,
            JsonArray().apply {
                add(
                    JsonMap().apply {
                        put("string", "hello")
                        put("number", 1)
                        put("boolean", false)
                    }
                )
            }
        )
    }

    @Test
    fun `matcher registration as class - observed is a polymorphic JsonArray`() {

        JsonMatcher.addMatcher("{{yolo}}", Yolo::class)

        assertionBuilder().jsonMatchesArray(
            """
                   {{yolo}} 
                """,
            JsonArray().apply {
                add(
                    JsonMap().apply {
                        put("common", "c1")
                        put("yolo1", "hello")
                    }
                )
                add(
                    JsonMap().apply {
                        put("common", "c2")
                        put("yolo2", "world")
                    }
                )
            }
        )
    }

    @Test
    fun `matcher registration as string - observed is a polymorphic JsonArray`() {

        JsonMatcher.addMatcher(
            "{{yolo}}", listOf(
                """{
                    "common": "c1",
                    "yolo1": "{{string}}"
                }""",
                """{
                    "common": "c2",
                    "yolo2": "{{string}}"
                }"""
            )
        )

        assertionBuilder().jsonMatchesArray(
            """
                   {{yolo}} 
                """,
            JsonArray().apply {
                add(
                    JsonMap().apply {
                        put("common", "c1")
                        put("yolo1", "hello")
                    }
                )
                add(
                    JsonMap().apply {
                        put("common", "c2")
                        put("yolo2", "world")
                    }
                )
            }
        )
    }

    @Test
    fun `matcher registration as class - observed is a polymorphic json array as string`() {

        JsonMatcher.addMatcher("{{yolo}}", Yolo::class)

        assertionBuilder().jsonMatchesArray(
            """
                   {{yolo}} 
                """,
            """[
                       {
                            "common": "c1",
                            "yolo1": "hello"
                       },
                       {
                            "common": "c2",
                            "yolo2": "world"
                       }
                    ]
                """
        )
    }

    @Test
    fun `matcher registration as string - observed is a polymorphic json array as string`() {

        JsonMatcher.addMatcher(
            "{{yolo}}", listOf(
                """{
                    "common": "c1",
                    "yolo1": "{{string}}"
                }""",
                """{
                    "common": "c2",
                    "yolo2": "{{string}}"
                }"""
            )
        )

        assertionBuilder().jsonMatchesArray(
            """
                   {{yolo}} 
                """,
            """[
                       {
                            "common": "c1",
                            "yolo1": "hello"
                       },
                       {
                            "common": "c2",
                            "yolo2": "world"
                       }
                    ]
                """
        )
    }

    @Test
    fun `matcher registration - polymorphism in arrays`() {

        JsonMatcher.addMatcher(
            "{{yolo}}", listOf(
                """{
                    "common": "c1",
                    "yolo1": "{{string}}"
                }""",
                """{
                    "common": "c2",
                    "yolo2": "{{string}}"
                }"""
            )
        )
        assertionBuilder().jsonMatchesObject(
            """
                   {
                        "data": "[[{{yolo?}}]]"
                   } 
                """,
            """
                   {
                        "data": [{
                            "common": "c1",
                            "yolo1": "1"
                        }, {
                            "common": "c2",
                            "yolo2": "2"
                        }, null]
                   } 
                """
        )
    }
}

data class TestDataObject(
    val string: String,
    val number: Int,
    val boolean: Boolean,
)


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "common"
)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = Yolo1::class, name = "c1"),
        JsonSubTypes.Type(value = Yolo2::class, name = "c2")
    ]
)
sealed class Yolo

class Yolo1(
    val yolo1: String,
) : Yolo()

class Yolo2(
    val yolo2: String,
) : Yolo()
