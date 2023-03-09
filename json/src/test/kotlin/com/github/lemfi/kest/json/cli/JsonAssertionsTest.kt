package com.github.lemfi.kest.json.cli

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.json.model.JsonArray
import com.github.lemfi.kest.json.model.JsonMap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle.STRICT
import kotlin.system.measureTimeMillis


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
        assertEquals(
            """{data=[1, 2, 3]} not found in array at "array"""",
            exception.message
        )
    }

    @Test
    fun `check array content without checking exact number of elements`() {

        assertionBuilder().jsonMatches("""[1, 2]""", """[1, 2, 3]""", checkExactCountOfArrayElements = false)

        assertionBuilder().jsonMatches(
            """
            {
                "data1": 12,
                "array": ["val1", "val3"],
                "data2": "a string"
            }
        """.trimIndent(), """
            {
                "data1": 12,
                "array": ["val1", "val2", "val3"],
                "data2": "a string"
            }
        """.trimIndent(), checkExactCountOfArrayElements = false
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
                        "hello": "hello"
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), """
            {
                "data1": 12,
                "array": [
                    {
                        "hello": "world"
                    },
                    {
                        "world": "hello"
                    },
                     {
                        "hello": "hello"
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), checkExactCountOfArrayElements = false
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
                        "data": [2, 3]
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), """
            {
                "data1": 12,
                "array": [
                    {
                        "hello": "world"
                    },
                    {
                        "blah": "blah"
                    },
                    {
                        "data": [1, 2, 3]
                    }
                    
                ],
                "data2": "a string"
            }
        """.trimIndent(), checkExactCountOfArrayElements = false
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
                        "hello": "world"
                    },
                    {
                        "data": [3, 3, 3]
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), checkArraysOrder = true, checkExactCountOfArrayElements = false,
            )
        }
        assertEquals(
            """{data=[1, 2, 3]} not found in array at "array"""",
            exception.message
        )
    }

    @Test
    fun `check array content without checking exact number of elements nor order of elements`() {

        assertionBuilder().jsonMatches("""[1, 2]""", """[1, 2, 3]""", checkExactCountOfArrayElements = false, checkArraysOrder = false)

        assertionBuilder().jsonMatches(
            """
            {
                "data1": 12,
                "array": ["val1", "val3"],
                "data2": "a string"
            }
        """.trimIndent(), """
            {
                "data1": 12,
                "array": ["val2", "val3", "val1"],
                "data2": "a string"
            }
        """.trimIndent(), checkExactCountOfArrayElements = false, checkArraysOrder = false
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
                        "hello": "hello"
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), """
            {
                "data1": 12,
                "array": [
                    {
                        "hello": "hello"
                    },
                    {
                        "hello": "world"
                    },
                    {
                        "world": "hello"
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), checkExactCountOfArrayElements = false, checkArraysOrder = false
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
                        "data": [2, 3]
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), """
            {
                "data1": 12,
                "array": [
                    {
                        "hello": "world"
                    },
                    {
                        "blah": "blah"
                    },
                    {
                        "data": [1, 3, 2]
                    }
                    
                ],
                "data2": "a string"
            }
        """.trimIndent(), checkExactCountOfArrayElements = false, checkArraysOrder = false
        )



        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
            {
                "data1": 12,
                "array": [
                    {
                        "data": [1, 2, 3]
                    },
                    {
                        "hello": "world"
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), """
            {
                "data1": 12,
                "array": [
                    {
                        "hello": "world"
                    },
                    {
                        "data": [3, 3, 3]
                    }
                ],
                "data2": "a string"
            }
        """.trimIndent(), checkArraysOrder = false, checkExactCountOfArrayElements = false,
            )
        }
        assertEquals(
            """{data=[1, 2, 3]} not found in array at "array"""",
            exception.message
        )
    }

    @Test
    fun `additional fields on observed json may be ignored - expected is a json object`() {
        val expected = """
                {
                    "hello": "world",
                    ${optionalJsonKey("who")}: "are you?",
                    ${optionalJsonKey("how")}: "are you?"
                }
            """.trimIndent()

        val observedWithoutOptional = """
                {
                    "hello": "world",
                    "why": "are you there?"
                }
            """.trimIndent()

        val observedWithOptional1 = """
                {
                    "hello": "world",
                    "who": "are you?",
                    "why": "are you there?"
                }
            """.trimIndent()

        val observedWithOptional2 = """
                {
                    "hello": "world",
                    "how": "are you?",
                    "why": "are you there?"
                }
            """.trimIndent()

        val observedWithBothOptional = """
                {
                    "hello": "world",
                    "who": "are you?",
                    "how": "are you?",
                    "why": "are you there?"
                }
            """.trimIndent()

        assertionBuilder().jsonMatches(
            expected = expected,
            observed = observedWithoutOptional,
            ignoreUnknownProperties = true
        )
        assertionBuilder().jsonMatches(
            expected = expected,
            observed = observedWithOptional1,
            ignoreUnknownProperties = true
        )
        assertionBuilder().jsonMatches(
            expected = expected,
            observed = observedWithOptional2,
            ignoreUnknownProperties = true
        )
        assertionBuilder().jsonMatches(
            expected = expected,
            observed = observedWithBothOptional,
            ignoreUnknownProperties = true
        )

        assertThrows<AssertionFailedError> {
            assertionBuilder().jsonMatches(
                expected = expected,
                observed = observedWithoutOptional,
                ignoreUnknownProperties = false
            )
        }

        assertThrows<AssertionFailedError> {
            assertionBuilder().jsonMatches(
                expected = expected,
                observed = observedWithOptional1,
                ignoreUnknownProperties = false
            )
        }

        assertThrows<AssertionFailedError> {
            assertionBuilder().jsonMatches(
                expected = expected,
                observed = observedWithOptional2,
                ignoreUnknownProperties = false
            )
        }

        assertThrows<AssertionFailedError> {
            assertionBuilder().jsonMatches(
                expected = expected,
                observed = observedWithBothOptional,
                ignoreUnknownProperties = false
            )
        }
    }

    @Test
    fun `additional fields on observed json may be ignored - expected is a string pattern`() {
        val dataPattern = pattern("data") definedBy """{"hello": "$stringPattern"}"""


        val expected = """$dataPattern"""
        val observed = """
                {
                    "hello": "world",
                    "how": "are you?"
                }
            """.trimIndent()

        assertionBuilder().jsonMatches(expected = expected, observed = observed, ignoreUnknownProperties = true)
        assertThrows<AssertionFailedError> {
            assertionBuilder().jsonMatches(
                expected = expected,
                observed = observed,
                ignoreUnknownProperties = false
            )
        }
    }

    @Test
    fun `additional fields on observed json may be ignored - expected is a class pattern`() {

        val dataPattern = pattern("data") definedBy TestDataObject::class

        val expected = dataPattern.toString()

        val observed = """
                {
                    "string": "a string",
                    "number": 2,
                    "boolean": false,
                    "hello": "world"
                }
            """.trimIndent()

        assertionBuilder().jsonMatches(expected = expected, observed = observed, ignoreUnknownProperties = true)
        assertThrows<AssertionFailedError> {
            assertionBuilder().jsonMatches(
                expected = expected,
                observed = observed,
                ignoreUnknownProperties = false
            )
        }
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
                            "bye": "${stringPattern.nullable}"
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
                        "string": "$stringPattern",
                        "number": "$numberPattern",
                        "boolean": "$booleanPattern",
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
                        "string": "$stringPattern",
                        "number": "$numberPattern",
                        "boolean": "$booleanPattern"
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
                        "string": "$stringPattern",
                        "number": "$numberPattern",
                        "boolean": "$booleanPattern"
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

        assertEquals(
            "missing entries for [{number=1, boolean=true, string=hello}], expected 2 entries, got 1 entries at ROOT",
            exception.message
        )
    }


    @Test
    fun `json object with sub object`() {

        assertionBuilder().jsonMatches(
            """
                   {
                        "string": {"key": "$stringPattern"},
                        "number": "$numberPattern",
                        "boolean": "$booleanPattern"
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
                        "strings": "${jsonArrayOf(stringPattern)}"
                   }
                """,
                """
                   {
                        "strings": ["hello", 1]
                   }
                """
            )
        }
        assertEquals("""expected class kotlin.String, got 1 at "strings[1]"""", exception1.message)


        val exception2 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                   {
                        "numbers": "${jsonArrayOf(numberPattern)}"
                   }
                """,
                """
                   {
                        "numbers": [1, "world"],
                   }
                """
            )
        }
        assertEquals("expected json object structure at ROOT", exception2.message)


        val exception3 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                   {
                        "booleans": "${jsonArrayOf(booleanPattern)}"
                   }
                """,
                """
                   {
                        "booleans": [true, "world", 1]
                   }
                """
            )
        }
        assertEquals(
            """expected object of type class kotlin.Boolean, got "world" at "booleans[1]"""",
            exception3.message
        )


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
        assertEquals("""expected "world", got "worlds" at "some strings[1]"""", exception4.message)


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
        assertEquals("""expected 2, got true at "some numbers[1]"""", exception5.message)

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
        assertEquals("""expected false, got 2 at "some booleans[1]"""", exception6.message)

    }

    @Test
    fun `json object with arrays of simple types`() {

        assertionBuilder().jsonMatches(
            """
                   {
                        "strings": "${jsonArrayOf(stringPattern)}",
                        "numbers": "${jsonArrayOf(numberPattern)}",
                        "booleans": "${jsonArrayOf(booleanPattern)}",
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
                        "string": "$stringPattern"
                   } 
                """,
                """
                   {
                        "string": null
                   } 
                """
            )
        }
        assertEquals("""expected none nullable value $stringPattern at "string"""", exception.message)

    }

    @Test
    fun `json array with not nullable pattern fails`() {

        val stringOrNumberPattern =
            pattern("stringornumber") definedBy listOf("${jsonArrayOf(stringPattern)}", "${jsonArrayOf(numberPattern)}")

        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                stringOrNumberPattern.pattern,
                """
                   ["12", null] 
                """
            )
        }
        assertEquals(
            """Failed to validate pattern, none of following patterns matched

--------
PATTERN
--------
 [[{{string}}]] => expected none nullable value {{string}} at "[1]"


--------
PATTERN
--------
 [[{{number}}]] => expected object of type class kotlin.Number, got "12" at "[0]"""", exception.message
        )

    }


    @Test
    fun `error message is correct for pattern of pattern`() {

        val p1 = pattern("p1") definedBy """
            {
                "a": "$stringPattern"
            }
        """.trimIndent()

        val p2 =
            pattern("p2") definedBy """
                {
                    "b": "$p1"
                }
            """.trimIndent()

        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                p2.pattern,
                """
                   {
                        "b": {
                            "a": 12
                        }
                   }
                        
                """
            )
        }
        assertEquals("""expected class kotlin.String, got 12 at "b" > "a"""", exception.message)

    }

    @Test
    fun `json array with multiple possible patterns`() {

        assertionBuilder().jsonMatches(
            listOf("${jsonArrayOf(stringPattern)}", "${jsonArrayOf(numberPattern)}"),
            """
                   ["12", "13"] 
                """
        )

        assertionBuilder().jsonMatches(
            listOf("${jsonArrayOf(stringPattern)}", "${jsonArrayOf(numberPattern)}"),
            """
                   [12, 13] 
                """
        )

    }

    @Test
    fun `json array with multiple possible patterns observed is JsonArray`() {

        assertionBuilder().jsonMatches(
            listOf("${jsonArrayOf(stringPattern)}", "${jsonArrayOf(numberPattern)}"),
            mutableListOf("12", "13")
        )

        assertionBuilder().jsonMatches(
            listOf("${jsonArrayOf(stringPattern)}", "${jsonArrayOf(numberPattern)}"),
            mutableListOf(12, 13)
        )

    }

    @Test
    fun `json int does not match when string requested`() {

        val exception1 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                   {
                        "string": "$stringPattern"
                   } 
                """,
                """
                   {
                        "string": 12
                   } 
                """
            )
        }

        assertEquals("""expected class kotlin.String, got 12 at "string"""", exception1.message)

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

        assertEquals("""Expected hello, got 12 at "string"""", exception2.message)

    }

    @Test
    fun `json object with nullable string passes`() {

        assertionBuilder().jsonMatches(
            """
                   {
                        "string": "${stringPattern.nullable}"
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
                        "number": "$numberPattern"
                   } 
                """,
                """
                   {
                        "number": null
                   } 
                """
            )
        }
        assertEquals("""expected none nullable value $numberPattern at "number"""", exception.message)

    }

    @Test
    fun `json object with nullable number passes`() {

        assertionBuilder().jsonMatches(
            """
                   {
                        "number": "${numberPattern.nullable}"
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
                        "boolean": "$booleanPattern"
                   } 
                """,
                """
                   {
                        "boolean": null
                   } 
                """
            )
        }
        assertEquals(
            """expected none nullable value $booleanPattern at "boolean"""", exception.message
        )

    }

    @Test
    fun `json object with nullable boolean passes`() {

        assertionBuilder().jsonMatches(
            """
                   {
                        "boolean": "${booleanPattern.nullable}"
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
                        "number": "$numberPattern"
                   } 
                """,
                """
                   {
                        "number": "1"
                   } 
                """
            )
        }
        assertEquals(
            """expected object of type class kotlin.Number, got "1" at "number"""",
            exception.message
        )

    }

    @Test
    fun `json object with boolean type not matching`() {


        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                        {
                            "boolean": "$booleanPattern"
                        } 
                    """,
                """
                       {
                            "boolean": "true"
                       } 
                    """
            )
        }
        assertEquals(
            """expected object of type class kotlin.Boolean, got "true" at "boolean"""",
            exception.message
        )

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
        assertEquals("""Expected 1234, got 5678 at "data"""", exception.message)

    }

    @Test
    fun `error path is correctly set on assertion failure message`() {

        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                        {
                            "level0": {
                                "level01": {
                                    "level012": [{
                                            "level01230": {
                                                "level012304": "level012304"
                                            }
                                        },
                                        {
                                            "level01231": {
                                                "level012314": "level012314"
                                            }
                                        },
                                        {
                                            "level01233": {
                                                "level012334": "level012324"
                                            }
                        
                                        }
                                    ]
                                },
                                "level11": {
                                    "level112": [{
                                            "level11230": {
                                                "level112304": "level112304"
                                            }
                                        },
                                        {
                                            "level11231": {
                                                "level112314": "level112314"
                                            }
                                        },
                                        {
                                            "level11233": {
                                                "level112334": "level112324"
                                            }
                        
                                        }
                                    ]
                                }
                            }
                        }
                    """,
                """
                       {
                            "level0": {
                                "level01": {
                                    "level012": [{
                                            "level01230": {
                                                "level012304": "level012304"
                                            }
                                        },
                                        {
                                            "level01231": {
                                                "level012314": "level012314"
                                            }
                                        },
                                        {
                                            "level01233": {
                                                "level012334": "level012324"
                                            }
                        
                                        }
                                    ]
                                },
                                "level11": {
                                    "level112": [{
                                            "level11230": {
                                                "level112304": "level112304"
                                            }
                                        },
                                        {
                                            "level11231": {
                                                "level112314": "error here"
                                            }
                                        },
                                        {
                                            "level11233": {
                                                "level112334": "level112324"
                                            }
                                        }
                                    ]
                                }
                            }
                        } 
                    """
            )
        }
        assertEquals(
            """Expected level112314, got error here at "level0" > "level11" > "level112[1]" > "level11231" > "level112314"""",
            exception.message
        )

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
        assertEquals("expected [data1] entries, got [data2] entries at ROOT", exception.message)

    }

    @Test
    fun `json array of simple types strings`() {

        assertionBuilder().jsonMatches(
            """
                    [[{
                        "string": "${stringPattern.nullable}",
                        "number": "${numberPattern.nullable}",
                        "boolean": "${booleanPattern.nullable}",
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

        val descDataPattern = pattern("descdata") definedBy """{"data": "$numberPattern"}"""

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": "${jsonArrayOf(descDataPattern)}"
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

        val descDataPattern = pattern("descdata") definedBy """{"data": "$numberPattern"}"""

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": "${jsonArrayOf(descDataPattern.nullable)}"
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

        val descDataPattern = pattern("descdata") definedBy """{"data": "$numberPattern"}"""

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": "${jsonArrayOf(descDataPattern.nullable).nullable}"
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

        val descDataPattern = pattern("descdata") definedBy """{"data": "$numberPattern"}"""

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": "${jsonArrayOf(descDataPattern.nullable).nullable}"
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

        val dataPattern = pattern("data") definedBy """{"data": "$stringPattern"}"""

        assertionBuilder().jsonMatches(
            """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": ["$dataPattern", "$dataPattern"]
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
                        "string": "${stringPattern.nullable}",
                        "array": ["$dataPattern", {"data": "world"}]
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
                        "string": "${stringPattern.nullable}",
                        "array": [{"data": "world"}, "$dataPattern"]
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
                        "string": "${stringPattern.nullable}",
                        "array": "${jsonArrayOf(dataPattern)}"
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

        val exception = assertThrows<IllegalStateException> {

            assertionBuilder().jsonMatches(
                """
                [[
                {
                    "string": "${stringPattern.nullable}"
                },
                {
                    "number": "$numberPattern"
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
        assertEquals(
            """wrong pattern [[
 {
     "string": "${stringPattern.nullable}"
 },
 {
     "number": "$numberPattern"
 }
]]""", exception.message
        )

    }

    @Test
    fun `json matcher of type function`() {

        fun patternFunction(data: String): Boolean {
            val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

            return try {
                dateFormatter.parse(data)
                true
            } catch (e: DateTimeParseException) {
                false
            }
        }

        val datePattern = pattern("date") definedBy ::patternFunction

        assertionBuilder().jsonMatches(
            """
                        {
                            "string": "${stringPattern.nullable}",
                            "date": "$datePattern",
                            "date_array": "${jsonArrayOf(datePattern)}"
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
                        "string": "${stringPattern.nullable}",
                        "date": "$datePattern"
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
        assertEquals(""""hello" does not validate pattern $datePattern at "date"""", exception.message)

    }

    @Test
    fun `json matcher of type function - nullable`() {

        val datePattern = pattern("date") definedBy { data: String ->
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
                        "string": "${stringPattern.nullable}",
                        "date": "${datePattern.nullable}"
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

        val datePattern = pattern("date") definedBy { data: String ->
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
                        "string": "${stringPattern.nullable}",
                        "date": "$datePattern"
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
        assertEquals("""expected none nullable value $datePattern at "date"""", exception.message)

    }

    @Test
    fun `json matcher of type function - array`() {

        val datePattern = pattern("date") definedBy { data: String ->
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
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern)}"
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
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern)}"
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
        assertEquals(
            """"bad format" does not validate pattern $datePattern at "date[1]"""",
            exception.message
        )
    }

    @Test
    fun `json matcher of type function - array not nullable`() {

        val datePattern = pattern("date") definedBy { data: String ->
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
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern)}"
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
        assertEquals("""expected none nullable value $datePattern at "date"""", exception1.message)


        val exception2 = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern)}"
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
        assertEquals("""expected none nullable value $datePattern at "date[1]"""", exception2.message)
    }

    @Test
    fun `json matcher of type function - nullable array`() {

        val datePattern = pattern("date") definedBy { data: String ->
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
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern).nullable}"
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

        val datePattern = pattern("date") definedBy { data: String ->
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
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern.nullable).nullable}"
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

        val myDataPattern = pattern("mydata") definedBy TestDataObject::class

        assertionBuilder().jsonMatches(
            """
                   $myDataPattern 
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

        val myDataPattern = pattern("mydata") definedBy TestDataObject::class

        assertionBuilder().jsonMatches(
            """
                   {
                        "data": "$myDataPattern"
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

        val myDataPattern = pattern("mydata") definedBy TestDataObject::class

        assertionBuilder().jsonMatches(
            """
                   {
                        "data": "${myDataPattern.nullable}"
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

        val myDataPattern = pattern("mydata") definedBy TestDataObject::class

        assertionBuilder().jsonMatches(
            """
                   $myDataPattern 
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
                   {"string": "$stringPattern", "number": "$numberPattern", "boolean": "$booleanPattern"} 
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

        val myDataPattern = pattern("mydata") definedBy TestDataObject::class

        assertionBuilder().jsonMatches(
            """
                ${jsonArrayOf(myDataPattern)}
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

        val myDataPattern = pattern("mydata") definedBy TestDataObject::class

        assertionBuilder().jsonMatches(
            """
                ${jsonArrayOf(myDataPattern)}
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

        val myDataPattern = pattern("mydata") definedBy TestDataObject::class

        assertionBuilder().jsonMatches(
            """
                {
                    "data": "${jsonArrayOf(myDataPattern)}"
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

        val myDataPattern = pattern("mydata") definedBy TestDataObject::class

        assertionBuilder().jsonMatches(
            """
                   {
                        "data": "${jsonArrayOf(myDataPattern.nullable)}"
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

        val myDataPattern = pattern("mydata") definedBy TestDataObject::class

        assertionBuilder().jsonMatches(
            """
                   ${jsonArrayOf(myDataPattern)} 
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

        val yoloPattern = pattern("yolo") definedBy Yolo::class

        assertionBuilder().jsonMatches(
            """
                   ${jsonArrayOf(yoloPattern)} 
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

        val yoloPattern = pattern("yolo") definedBy
                listOf(
                    """{
                    "common": "c1",
                    "yolo1": "$stringPattern"
                }""",
                    """{
                    "common": "c2",
                    "yolo2": "$stringPattern"
                }"""
                )

        assertionBuilder().jsonMatches(
            """${jsonArrayOf(yoloPattern)}""",
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

        val yoloPattern = pattern("yolo") definedBy Yolo::class

        assertionBuilder().jsonMatches(
            """
                  ${jsonArrayOf(yoloPattern)}
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

        val yoloPattern = pattern("yolo") definedBy
                listOf(
                    """{
                    "common": "c1",
                    "yolo1": "$stringPattern"
                }""",
                    """{
                    "common": "c2",
                    "yolo2": "$stringPattern"
                }"""
                )

        assertionBuilder().jsonMatches(
            """
                   ${jsonArrayOf(yoloPattern)}
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

        val yoloPattern = pattern("yolo") definedBy listOf(
            """{
                    "common": "c1",
                    "yolo1": "$stringPattern"
                }""",
            """{
                    "common": "c2",
                    "yolo2": "$stringPattern"
                }"""
        )

        assertionBuilder().jsonMatches(
            """
                   {
                        "data": "${jsonArrayOf(yoloPattern.nullable)}"
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

    @Test
    fun `when check array order is disabled, check must go fast enough`() {
        val data1 = """
            {
                "toto": [${
            (1..1000).joinToString(",") {
                """{
                        "t": "$it"
                    }"""
            }
        }
                ]
            }
        """.trimIndent()

        val time = measureTimeMillis {
            assertionBuilder().jsonMatches(data1, data1, false)
        }
        Assertions.assertTrue(time < 1000)
    }

    @Test
    fun `keys may be absent from JSON`() {
        val expected = """{
                "'MAYNOTBEPRESENT'[0-1]": "$stringPattern",
                "data": "$stringPattern"
            }""".trimIndent()

        val observedPresent = """
                {
                    "MAYNOTBEPRESENT": "a string",
                    "data": "world"
                }
            """.trimIndent()

        val observedAbsent = """
                {
                    "data": "world"
                }
            """.trimIndent()

        assertionBuilder().jsonMatches(expected = expected, observed = observedPresent, ignoreUnknownProperties = true)
        assertionBuilder().jsonMatches(expected = expected, observed = observedPresent, ignoreUnknownProperties = false)
        assertionBuilder().jsonMatches(expected = expected, observed = observedAbsent, ignoreUnknownProperties = true)
        assertionBuilder().jsonMatches(expected = expected, observed = observedAbsent, ignoreUnknownProperties = false)
    }

    @Test
    fun `sort attributes when expeted pattern and observed data do not have same attributes`() {

        val expected = """
            {
                "attr0": "$stringPattern",
                "attr1": "$stringPattern",
                "attr2": "$stringPattern",
                "attr3": "$stringPattern",
                "attr4": "$stringPattern",
                "attr5": "$stringPattern",
                "attr6": "$stringPattern",
                "attr7": "$stringPattern",
                "attr8": "$stringPattern",
                "attr9": "$stringPattern"
            }
        """

        val observed = """
            {
                "attr8": "$stringPattern",
                "attrb": "$stringPattern",
                "attr2": "$stringPattern",
                "attr9": "$stringPattern",
                "attr4": "$stringPattern",
                "attra": "$stringPattern",
                "attr5": "$stringPattern",
                "attr7": "$stringPattern",
                "attr1": "$stringPattern",
                "attr3": "$stringPattern",
                "attrc": "$stringPattern"
            }
        """

        val exception = assertThrows<AssertionFailedError> {
            assertionBuilder().jsonMatches(expected, observed)
        }

        assertEquals(
            "expected [attr0, attr1, attr2, attr3, attr4, attr5, attr6, attr7, attr8, attr9] entries, got [attr1, attr2, attr3, attr4, attr5, attr7, attr8, attr9, attra, attrb, attrc] entries at ROOT",
            exception.message
        )
    }

    @Test
    fun `optional json key are not in expected error message when not in observed`() {

        val exception = assertThrows<AssertionFailedError> {

            assertionBuilder().jsonMatches(
                """
                   {
                        ${optionalJsonKey("optKey1")}: "$stringPattern",
                        "manKey2": "$stringPattern",
                        "manKey1": "$stringPattern",
                        ${optionalJsonKey("optKey2")}: "$stringPattern"
                   } 
                """,
                """
                   {
                        "optKey2": "12",
                        "manKey1": "12"
                   } 
                """
            )
        }

        assertEquals(
            """expected [manKey1, manKey2, optKey2, optional(optKey1)] entries, got [manKey1, optKey2] entries at ROOT""",
            exception.message
        )
    }

    @Test
    fun `unclear error message when validating a pattern on an element of an array when pattern is not well-formed json`() {

        val expected = listOf(
            """
                {
                    "a": "$stringPattern",  
                    "b": "$stringPattern"
                } 
            """,
            """
                
                    "c": "$stringPattern",  
                    "d": "$stringPattern"
                
            """
        )

        val observed = """
            {
                "e": "$stringPattern",
                "f": "$stringPattern"
            }
        """

        val exception = assertThrows<AssertionFailedError> {
            assertionBuilder().jsonMatches(expected, observed)
        }

        assertEquals(
            """Failed to validate pattern, none of following patterns matched

--------
PATTERN
--------
 
                {
                    "a": "{{string}}",  
                    "b": "{{string}}"
                } 
             => expected [a, b] entries, got [e, f] entries at ROOT


--------
PATTERN
--------
 
                
                    "c": "{{string}}",  
                    "d": "{{string}}"
                
             => expected 
                
                    "c": "{{string}}",  
                    "d": "{{string}}"
                
            , got 
            {
                "e": "{{string}}",
                "f": "{{string}}"
            }
         at ROOT""",
            exception.message
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
