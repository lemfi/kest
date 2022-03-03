package com.github.lemfi.kest.json.cli

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.json.model.JsonArray
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.json.model.KestArray
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle.STRICT


private fun assertionBuilder() = AssertionsBuilder("json test", null)

class JsonAssertionsTest {

    @Test
    fun `check array content regardless of sorting`() {

        assertionBuilder().jsonMatches("""[1, 2, 3]""", """[3, 2, 1]""", checkArraysOrder = false)

        assertionBuilder().jsonMatches(
            """
            {
                "data1": 12,
                "array": ["val1", "val2"],
                "data2": "a string"
            }
        """.trimIndent(), """
            {
                "data1": 12,
                "array": ["val2", "val1"],
                "data2": "a string"
            }
        """.trimIndent(), checkArraysOrder = false
        )

        assertionBuilder().jsonMatches(
            """
            {
                "data1": 12,
                "array": [
                    {
                        "hello": "world"
                    },
                    {
                        "world": "hello"
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), """
            {
                "data1": 12,
                "array": [
                    {
                        "world": "hello"
                    },
                     {
                        "hello": "world"
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), checkArraysOrder = false
        )

        assertionBuilder().jsonMatches(
            """
            {
                "data1": 12,
                "array": [
                    {
                        "hello": "world"
                    },
                    {
                        "data": [1, 2, 3]
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), """
            {
                "data1": 12,
                "array": [
                    {
                        "data": [3, 2, 1]
                    },
                     {
                        "hello": "world"
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), checkArraysOrder = false
        )

        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
            {
                "data1": 12,
                "array": [
                    {
                        "hello": "world"
                    },
                    {
                        "data": [1, 2, 3]
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), """
            {
                "data1": 12,
                "array": [
                    {
                        "data": [3, 3, 3]
                    },
                     {
                        "hello": "world"
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), checkArraysOrder = false
            )
        }
        Assertions.assertEquals("{data=[3, 3, 3]} is not an expected element of array", exception.message)
    }

    @Test
    fun `array containing object with nullable values`() {
        assertionBuilder().jsonMatches(
            """
                    {
                        "data": [{
                            "hello": "world",
                            "bye": null
                        }]
                    }
                    """,
            """{
                        "data": [{
                            "hello": "world",
                            "bye": null
                        }]
                    }"""
        )
        assertionBuilder().jsonMatches(
            """
                    {
                        "data": [{
                            "hello": "world",
                            "bye": "{{string?}}"
                        }]
                    }
                    """,
            """{
                        "data": [{
                            "hello": "world",
                            "bye": null
                        }]
                    }"""
        )
    }

    @Test
    fun `json object with simple types that matches`() {

        assertionBuilder().jsonMatches(
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
    fun `json array with simple types that matches`() {

        assertionBuilder().jsonMatches(
            """
                   [{
                        "string": "{{string}}",
                        "number": "{{number}}",
                        "boolean": "{{boolean}}"
                    }, {
                        "astring": "hello",
                        "anumber": 1,
                        "aboolean": false
                    }]
                """,
            """
                [{
                		"string": "hello",
                		"number": 1,
                		"boolean": true
                	},
                	{
                		"astring": "hello",
                		"anumber": 1,
                		"aboolean": false
                	}
                ]
            """
        )
    }

    @Test
    fun `json array missing one entry`() {

        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                   [{
                        "string": "{{string}}",
                        "number": "{{number}}",
                        "boolean": "{{boolean}}"
                    }, {
                        "astring": "hello",
                        "anumber": 1,
                        "aboolean": false
                    }]
                """,
                """
                [{
                		"string": "hello",
                		"number": 1,
                		"boolean": true
                	}
                ]
            """
            )
        }

        Assertions.assertEquals(
            "missing entries for [{number=1, boolean=true, string=hello}], expected 2 entries, got 1 entries",
            exception.message
        )
    }


    @Test
    fun `json object with sub object`() {

        assertionBuilder().jsonMatches(
            """
                   {
                        "string": {"key": "{{string}}"},
                        "number": "{{number}}",
                        "boolean": "{{boolean}}"
                    }
                """,
            """
                {
                    "string": {"key": "hello"},
                    "number": 1,
                    "boolean": true
                }
                
            """
        )
    }


    @Test
    fun `json object with arrays of simple types with errors`() {

        val exception1 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("expected class kotlin.String, got 1", exception1.message)


        val exception2 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("expected json object structure", exception2.message)


        val exception3 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("""expected object of type class kotlin.Boolean, got "world"""", exception3.message)


        val exception4 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("""expected "world", got "worlds"""", exception4.message)


        val exception5 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("expected 2, got true", exception5.message)

        val exception6 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("expected false, got 2", exception6.message)

    }

    @Test
    fun `json object with arrays of simple types`() {

        assertionBuilder().jsonMatches(
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


        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("expected none nullable value {{string}}", exception.message)

    }

    @Test
    fun `json array with not nullable pattern fails`() {

        `add json matcher`("{{stringornumber}}", listOf("[[{{string}}]]", "[[{{number}}]]"))

        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                   {{stringornumber}}
                """,
                """
                   ["12", null] 
                """
            )
        }
        Assertions.assertEquals("""expected object of type class kotlin.Number, got "12"""", exception.message)

    }

    @Test
    fun `json array with multiple possible patterns`() {

        assertionBuilder().jsonMatches(
            listOf("[[{{string}}]]", "[[{{number}}]]"),
            """
                   ["12", "13"] 
                """
        )

        assertionBuilder().jsonMatches(
            listOf("[[{{string}}]]", "[[{{number}}]]"),
            """
                   [12, 13] 
                """
        )

    }

    @Test
    fun `json array with multiple possible patterns observed is JsonArray`() {

        assertionBuilder().jsonMatches(
            listOf("[[{{string}}]]", "[[{{number}}]]"),
            KestArray<String>().apply {
                add("12")
                add("13")
            }
        )

        assertionBuilder().jsonMatches(
            listOf("[[{{string}}]]", "[[{{number}}]]"),
            KestArray<Int>().apply {
                add(12)
                add(13)
            }
        )

    }

    @Test
    fun `json int does not match when string requested`() {

        val exception1 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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

        Assertions.assertEquals("expected class kotlin.String, got 12", exception1.message)

        val exception2 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                   {
                        "string": "hello"
                   } 
                """,
                """
                   {
                        "string": 12
                   } 
                """
            )
        }

        Assertions.assertEquals("""Expected hello, got 12""", exception2.message)

    }

    @Test
    fun `json object with nullable string passes`() {

        assertionBuilder().jsonMatches(
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


        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("expected none nullable value {{number}}", exception.message)

    }

    @Test
    fun `json object with nullable number passes`() {

        assertionBuilder().jsonMatches(
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


        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals(
            "expected none nullable value {{boolean}}", exception.message
        )

    }

    @Test
    fun `json object with nullable boolean passes`() {

        assertionBuilder().jsonMatches(
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


        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("""expected object of type class kotlin.Number, got "1"""", exception.message)

    }

    @Test
    fun `json object with boolean type not matching`() {


        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("""expected object of type class kotlin.Boolean, got "true"""", exception.message)

    }

    @Test
    fun `json object with value not matching`() {


        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("""Expected 1234, got 5678""", exception.message)

    }

    @Test
    fun `json object with key not matching`() {

        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
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
        Assertions.assertEquals("expected [data1] entries, got [data2] entries", exception.message)

    }

    @Test
    fun `json array of simple types strings`() {

        assertionBuilder().jsonMatches(
            """
                    [[{
                        "string": "{{string?}}",
                        "number": "{{number?}}",
                        "boolean": "{{boolean?}}",
                        "astring": "hello",
                        "anumber": 1,
                        "aboolean": false
                   }]] 
                """,
            """
                   [
                        {
                            "string": "hello",
                            "number": 1,
                            "boolean": true,
                            "astring": "hello",
                            "anumber": 1,
                            "aboolean": false
                       },
                       {
                            "string": null,
                            "number": null,
                            "boolean": null,
                            "astring": "hello",
                            "anumber": 1,
                            "aboolean": false
                       }
                   ] 
                """
        )
    }

    @Test
    fun `json array of object in subtype`() {

        `add json matcher`("{{descdata}}", """{"data": "{{number}}"}""")

        assertionBuilder().jsonMatches(
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

        `add json matcher`("{{descdata}}", """{"data": "{{number}}"}""")

        assertionBuilder().jsonMatches(
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
    fun `json nullable array of nullable objects in subtypes - array null`() {

        `add json matcher`("{{descdata}}", """{"data": "{{number}}"}""")

        assertionBuilder().jsonMatches(
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
    fun `json nullable array of nullable objects in subtypes - objects null`() {

        `add json matcher`("{{descdata}}", """{"data": "{{number}}"}""")

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "{{string?}}",
                        "array": "[[{{descdata?}}]]?"
                   } 
                """,
            """
                        {
                            "string": "hello",
                            "array": [null, null]
                        }
                """
        )
    }

    @Test
    fun `array pattern or array of patterns`() {

        `add json matcher`("{{data}}", """{"data": "{{string}}"}""")

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "{{string?}}",
                        "array": ["{{data}}", "{{data}}"]
                   } 
                """,
            """
                        {
                            "string": "hello",
                            "array": [{"data": "hello"}, {"data": "world"}]
                        }
                """
        )

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "{{string?}}",
                        "array": ["{{data}}", {"data": "world"}]
                   } 
                """,
            """
                        {
                            "string": "hello",
                            "array": [{"data": "hello"}, {"data": "world"}]
                        }
                """
        )

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "{{string?}}",
                        "array": [{"data": "world"}, "{{data}}"]
                   } 
                """,
            """
                        {
                            "string": "hello",
                            "array": [{"data": "hello"}, {"data": "world"}]
                        }
                """,
            checkArraysOrder = false
        )

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "{{string?}}",
                        "array": "[[{{data}}]]"
                   } 
                """,
            """
                        {
                            "string": "hello",
                            "array": [{"data": "hello"}, {"data": "world"}]
                        }
                """
        )
    }

    @Test
    fun `json array patterns error`() {

        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                [[
                {
                    "string": "{{string?}}"
                },
                {
                    "number": "{{number}}"
                }
               ]]
                """,
                """
                [
                    {
                        "string": "hello"
                    },
                    {
                        "string": "world"
                    },
                    {
                        "number": 32
                    },
                    {
                        "boolean": true
                    }
                ]
                """
            )
        }
        Assertions.assertEquals("expected [string] entries, got [number] entries", exception.message)

    }

    @Test
    fun `json matcher of type function`() {

        `add json matcher`("{{date}}", String::class) { data ->
            val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

            try {
                dateFormatter.parse(data)
                true
            } catch (e: DateTimeParseException) {
                false
            }
        }

        assertionBuilder().jsonMatches(
            """
                        {
                            "string": "{{string?}}",
                            "date": "{{date}}",
                            "date_array": "[[{{date}}]]"
                       } 
                    """,
            """
                        {
                            "string": "hello",
                            "date": "2021-01-12",
                            "date_array": ["2021-01-12", "2021-01-13"]
                        }
                    """
        )

        val exception = assertThrows<AssertionFailedError> {
            assertionBuilder().jsonMatches(
                """
                    {
                        "string": "{{string?}}",
                        "date": "{{date}}"
                   } 
                """,
                """
                        {
                            "string": "hello",
                            "date": "hello"
                        }
                """
            )
        }
        Assertions.assertEquals(""""hello" does not validate pattern {{date}}""", exception.message)

    }

    @Test
    fun `json matcher of type function - nullable`() {

        `add json matcher`("{{date}}", String::class) { data ->
            val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

            try {
                dateFormatter.parse(data)
                true
            } catch (e: DateTimeParseException) {
                false
            }
        }

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "{{string?}}",
                        "date": "{{date?}}"
                   } 
                """,
            """
                        {
                            "string": "hello",
                            "date": null
                        }
                """
        )
    }

    @Test
    fun `json matcher of type function - not nullable`() {

        `add json matcher`("{{date}}", String::class) { data ->
            val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

            try {
                dateFormatter.parse(data)
                true
            } catch (e: DateTimeParseException) {
                false
            }
        }

        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                    {
                        "string": "{{string?}}",
                        "date": "{{date}}"
                   } 
                """,
                """
                        {
                            "string": "hello",
                            "date": null
                        }
                """
            )
        }
        Assertions.assertEquals("expected none nullable value {{date}}", exception.message)

    }

    @Test
    fun `json matcher of type function - array`() {

        `add json matcher`("{{date}}", String::class) { data ->
            val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

            try {
                dateFormatter.parse(data)
                true
            } catch (e: DateTimeParseException) {
                false
            }
        }

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "{{string?}}",
                        "date": "[[{{date}}]]"
                   }
                """,
            """
                        {
                            "string": "hello",
                            "date": ["2012-09-13", "2001-08-24"]
                        }
                """
        )

        val exception = assertThrows<AssertionFailedError> {
            assertionBuilder().jsonMatches(
                """
                    {
                        "string": "{{string?}}",
                        "date": "[[{{date}}]]"
                   } 
                """,
                """
                        {
                            "string": "hello",
                            "date": ["2012-09-13", "bad format"]
                        }
                """
            )
        }
        Assertions.assertEquals(""""bad format" does not validate pattern {{date}}""", exception.message)
    }

    @Test
    fun `json matcher of type function - array not nullable`() {

        `add json matcher`("{{date}}", String::class) { data ->
            val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

            try {
                dateFormatter.parse(data)
                true
            } catch (e: DateTimeParseException) {
                false
            }
        }

        val exception1 = assertThrows<AssertionFailedError> {


            assertionBuilder().jsonMatches(
                """
                    {
                        "string": "{{string?}}",
                        "date": "[[{{date}}]]"
                   }
                """,
                """
                        {
                            "string": "hello",
                            "date": null
                        }
                """
            )
        }
        Assertions.assertEquals("expected none nullable value {{date}}", exception1.message)


        val exception2 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                    {
                        "string": "{{string?}}",
                        "date": "[[{{date}}]]"
                   }
                """,
                """
                        {
                            "string": "hello",
                            "date": ["2020-12-13", null]
                        }
                """
            )
        }
        Assertions.assertEquals("expected none nullable value {{date}}", exception2.message)
    }

    @Test
    fun `json matcher of type function - nullable array`() {

        `add json matcher`("{{date}}", String::class) { data ->
            val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

            try {
                dateFormatter.parse(data)
                true
            } catch (e: DateTimeParseException) {
                false
            }
        }

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "{{string?}}",
                        "date": "[[{{date}}]]?"
                   } 
                """,
            """
                        {
                            "string": "hello",
                            "date": null
                        }
                """
        )
    }

    @Test
    fun `json matcher of type function - nullable array of nullable elements`() {

        `add json matcher`("{{date}}", String::class) { data ->
            val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

            try {
                dateFormatter.parse(data)
                true
            } catch (e: DateTimeParseException) {
                false
            }
        }

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "{{string?}}",
                        "date": "[[{{date?}}]]?"
                   } 
                """,
            """
                        {
                            "string": "hello",
                            "date": ["2014-07-23", null]
                        }
                """
        )
    }

    @Test
    fun `matcher registration - observed is string`() {

        `add json matcher`("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatches(
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

        `add json matcher`("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatches(
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

        `add json matcher`("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatches(
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

        `add json matcher`("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatches(
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
    fun `multiple possible patterns - observed is JsonMap`() {

        assertionBuilder().jsonMatches(
            listOf(
                """
                   {"hello": "world"}
                """,

                """
                   {"string": "{{string}}", "number": "{{number}}", "boolean": "{{boolean}}"} 
                """
            ),
            JsonMap().apply {
                put("string", "hello")
                put("number", 1)
                put("boolean", false)
            }
        )
    }

    @Test
    fun `matcher registration - observed an array displayed as string`() {

        `add json matcher`("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatches(
            """
                [[{{mydata}}]]
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
    fun `matcher registration - observed an array displayed as JsonArray`() {

        `add json matcher`("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatches(
            """
                [[{{mydata}}]]
            """,
            JsonArray().apply {
                add(JsonMap().apply {
                    put("string", "hello")
                    put("number", 1)
                    put("boolean", false)
                })
            }
        )
    }

    @Test
    fun `matcher registration - matcher in subtype as array`() {

        `add json matcher`("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatches(
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

        `add json matcher`("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatches(
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

        `add json matcher`("{{mydata}}", TestDataObject::class)

        assertionBuilder().jsonMatches(
            """
                   [[{{mydata}}]] 
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

        `add json matcher`("{{yolo}}", Yolo::class)

        assertionBuilder().jsonMatches(
            """
                   [[{{yolo}}]] 
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

        `add json matcher`(
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

        assertionBuilder().jsonMatches(
            """[[{{yolo}}]]""",
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

        `add json matcher`("{{yolo}}", Yolo::class)

        assertionBuilder().jsonMatches(
            """
                   [[{{yolo}}]] 
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

        `add json matcher`(
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

        assertionBuilder().jsonMatches(
            """
                   [[{{yolo}}]] 
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

        `add json matcher`(
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
        assertionBuilder().jsonMatches(
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
    @Suppress("unused") val yolo1: String,
) : Yolo()

class Yolo2(
    @Suppress("unused") val yolo2: String,
) : Yolo()
