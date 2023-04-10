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

        with(assertionBuilder()) {

            json("""[3, 2, 1]""") matches validator(checkArraysOrder = false) { """[1, 2, 3]""" }

            json(
                """
                    {
                        "data1": 12,
                        "array": ["val2", "val1"],
                        "data2": "a string"
                    }
                """
            ) matches validator(checkArraysOrder = false) {
                """
                {
                    "data1": 12,
                    "array": ["val1", "val2"],
                    "data2": "a string"
                }
                """
            }

            json(
                """
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
                """
            ) matches validator(checkArraysOrder = false) {
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
                """
            }

            json(
                """
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
                """
            ) matches validator(checkArraysOrder = false) {
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
                """
            }

            val exception = assertThrows<AssertionFailedError> {

                json(
                    """
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
                    """
                ) matches validator(checkArraysOrder = false) {
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
                    """
                }
            }

            assertEquals(
                """{data=[1, 2, 3]} not found in array at "array"""",
                exception.message
            )
        }
    }

    @Test
    fun `check array content without checking exact number of elements`() {

        with(assertionBuilder()) {

            json("""[1, 2, 3]""") matches validator(checkExactCountOfArrayElements = false) { """[1, 2]""" }

            json(
                """
                {
                    "data1": 12,
                    "array": ["val1", "val2", "val3"],
                    "data2": "a string"
                }
                """
            ) matches validator(checkExactCountOfArrayElements = false) {
                """
                    {
                        "data1": 12,
                        "array": ["val1", "val3"],
                        "data2": "a string"
                    }
                """
            }

            json(
                """
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
                """
            ) matches validator(checkExactCountOfArrayElements = false) {
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
                """
            }

            json(
                """
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
                """
            ) matches validator(checkExactCountOfArrayElements = false) {
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
                """
            }

            val exception = assertThrows<AssertionFailedError> {

                json(
                    """
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
                    """
                ) matches validator(checkArraysOrder = true, checkExactCountOfArrayElements = false) {
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
                    """
                }
            }
            assertEquals(
                """{data=[1, 2, 3]} not found in array at "array"""",
                exception.message
            )
        }
    }

    @Test
    fun `check array content without checking exact number of elements nor order of elements`() {

        with(assertionBuilder()) {


            json(
                """[1, 2, 3]"""
            ) matches validator(checkArraysOrder = false, checkExactCountOfArrayElements = false) {
                """[1, 2]"""
            }

            json(
                """
                    {
                        "data1": 12,
                        "array": ["val2", "val3", "val1"],
                        "data2": "a string"
                    }
                """
            ) matches validator(checkArraysOrder = false, checkExactCountOfArrayElements = false) {
                """
                    {
                        "data1": 12,
                        "array": ["val1", "val3"],
                        "data2": "a string"
                    }
                """
            }

            json(
                """
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
                """
            ) matches validator(checkArraysOrder = false, checkExactCountOfArrayElements = false) {
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
                """
            }

            json(
                """
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
                """
            ) matches validator(checkArraysOrder = false, checkExactCountOfArrayElements = false) {
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
                """
            }

            val exception = assertThrows<AssertionFailedError> {

                json(
                    """
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
                    """
                ) matches validator(checkArraysOrder = false, checkExactCountOfArrayElements = false) {
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
                    """
                }
            }
            assertEquals(
                """{data=[1, 2, 3]} not found in array at "array"""",
                exception.message
            )
        }
    }

    @Test
    fun `additional fields on observed json may be ignored - expected is a json object`() {

        with(assertionBuilder()) {


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

            json(observedWithoutOptional) matches validator(ignoreUnknownProperties = true) { expected }
            json(observedWithOptional1) matches validator(ignoreUnknownProperties = true) { expected }
            json(observedWithOptional2) matches validator(ignoreUnknownProperties = true) { expected }
            json(observedWithBothOptional) matches validator(ignoreUnknownProperties = true) { expected }

            assertThrows<AssertionFailedError> {
                json(observedWithoutOptional) matches validator(ignoreUnknownProperties = false) { expected }
            }

            assertThrows<AssertionFailedError> {
                json(observedWithOptional1) matches validator(ignoreUnknownProperties = false) { expected }
            }

            assertThrows<AssertionFailedError> {
                json(observedWithOptional2) matches validator(ignoreUnknownProperties = false) { expected }
            }

            assertThrows<AssertionFailedError> {
                json(observedWithBothOptional) matches validator(ignoreUnknownProperties = false) { expected }
            }
        }
    }

    @Test
    fun `additional fields on observed json may be ignored - expected is a string pattern`() {

        with(assertionBuilder()) {


            val dataPattern = pattern("data") definedBy """{"hello": "$stringPattern"}"""

            val expected = """$dataPattern"""
            val observed = """
                {
                    "hello": "world",
                    "how": "are you?"
                }
            """.trimIndent()

            json(observed) matches validator(ignoreUnknownProperties = true) { expected }
            assertThrows<AssertionFailedError> {
                json(observed) matches validator(ignoreUnknownProperties = false) { expected }
            }
        }
    }

    @Test
    fun `additional fields on observed json may be ignored - expected is a class pattern`() {

        with(assertionBuilder()) {

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

            json(observed) matches validator(ignoreUnknownProperties = true) { expected }
            assertThrows<AssertionFailedError> {
                json(observed) matches validator(ignoreUnknownProperties = false) { expected }
            }
        }
    }

    @Test
    fun `array containing object with nullable values`() {
        with(assertionBuilder()) {

            json(
                """{
                            "data": [{
                                "hello": "world",
                                "bye": null
                            }]
                        }"""
            ) matches validator {
                """
                    {
                        "data": [{
                            "hello": "world",
                            "bye": null
                        }]
                    }
                    """
            }

            json(
                """{
                            "data": [{
                                "hello": "world",
                                "bye": null
                            }]
                        }"""
            ) matches validator {
                """
                    {
                        "data": [{
                            "hello": "world",
                            "bye": "${stringPattern.nullable}"
                        }]
                    }
                    """
            }
        }
    }

    @Test
    fun `json object with simple types that matches`() {
        with(assertionBuilder()) {
            json(
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
            ) matches validator {
                """
                   {
                        "string": "$stringPattern",
                        "number": "$numberPattern",
                        "boolean": "$booleanPattern",
                        "a string": "hello",
                        "a number": 1,
                        "a boolean": false
                   } 
                """
            }
        }
    }


    @Test
    fun `json array with simple types that matches`() {

        with(assertionBuilder()) {
            json(
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
            ) matches validator {
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
                """
            }
        }
    }

    @Test
    fun `json array missing one entry`() {

        with(assertionBuilder()) {

            val exception = assertThrows<AssertionFailedError> {

                json(
                    """
                        [{
                                "string": "hello",
                                "number": 1,
                                "boolean": true
                            }
                        ]
                    """
                ) matches validator {
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
                """
                }
            }

            assertEquals(
                "missing entries for [{number=1, boolean=true, string=hello}], expected 2 entries, got 1 entries at ROOT",
                exception.message
            )
        }
    }


    @Test
    fun `json object with sub object`() {
        with(assertionBuilder()) {

            json(
                """
                    {
                        "string": {"key": "hello"},
                        "number": 1,
                        "boolean": true
                    }
                    
                """
            ) matches validator {
                """
                   {
                        "string": {"key": "$stringPattern"},
                        "number": "$numberPattern",
                        "boolean": "$booleanPattern"
                    }
                """
            }
        }
    }


    @Test
    fun `json object with arrays of simple types with errors`() {
        with(assertionBuilder()) {

            val exception1 = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "strings": ["hello", 1]
                           }
                        """
                ) matches validator {
                    """
                   {
                        "strings": "${jsonArrayOf(stringPattern)}"
                   }
                """
                }
            }
            assertEquals("""expected class kotlin.String, got 1 at "strings[1]"""", exception1.message)


            val exception2 = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "numbers": [1, "world"],
                           }
                        """
                ) matches validator {
                    """
                   {
                        "numbers": "${jsonArrayOf(numberPattern)}"
                   }
                """
                }
            }
            assertEquals("expected json object structure at ROOT", exception2.message)


            val exception3 = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "booleans": [true, "world", 1]
                           }
                        """
                ) matches validator {
                    """
                   {
                        "booleans": "${jsonArrayOf(booleanPattern)}"
                   }
                """
                }
            }
            assertEquals(
                """expected object of type class kotlin.Boolean, got "world" at "booleans[1]"""",
                exception3.message
            )


            val exception4 = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "some strings": ["hello", "worlds"]
                           }
                        """
                ) matches validator {
                    """
                   {
                        "some strings": ["hello", "world"]
                   }
                """
                }
            }
            assertEquals("""expected "world", got "worlds" at "some strings[1]"""", exception4.message)


            val exception5 = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "some numbers": [1, true]
                           } 
                        """
                ) matches validator {
                    """
                   {
                        "some numbers": [1, 2]
                   } 
                """
                }
            }
            assertEquals("""expected 2, got true at "some numbers[1]"""", exception5.message)

            val exception6 = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "some booleans": [true, 2]
                           }
                        """
                ) matches validator {
                    """
                   {
                        "some booleans": [true, false]
                   }
                """
                }
            }
            assertEquals("""expected false, got 2 at "some booleans[1]"""", exception6.message)

        }
    }

    @Test
    fun `json object with arrays of simple types`() {
        with(assertionBuilder()) {

            json(
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
            ) matches validator {
                """
                   {
                        "strings": "${jsonArrayOf(stringPattern)}",
                        "numbers": "${jsonArrayOf(numberPattern)}",
                        "booleans": "${jsonArrayOf(booleanPattern)}",
                        "some strings": ["hello", "world"],
                        "some numbers": [1, 2],
                        "some booleans": [true, false]
                   } 
                """
            }
        }
    }

    @Test
    fun `json object with not nullable string fails`() {
        with(assertionBuilder()) {


            val exception = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "string": null
                           } 
                        """
                ) matches validator {
                    """
                       {
                            "string": "$stringPattern"
                       } 
                    """
                }
            }
            assertEquals("""expected none nullable value $stringPattern at "string"""", exception.message)

        }
    }

    @Test
    fun `json array with not nullable pattern fails`() {
        with(assertionBuilder()) {

            val stringOrNumberPattern =
                pattern("stringornumber") definedBy listOf(
                    "${jsonArrayOf(stringPattern)}",
                    "${jsonArrayOf(numberPattern)}"
                )

            val exception = assertThrows<AssertionFailedError> {

                json(""" ["12", null] """) matches stringOrNumberPattern
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
    }


    @Test
    fun `error message is correct for pattern of pattern`() {
        with(assertionBuilder()) {

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

                json(
                    """
                           {
                                "b": {
                                    "a": 12
                                }
                           }
                                
                        """
                ) matches p2
            }
            assertEquals("""expected class kotlin.String, got 12 at "b" > "a"""", exception.message)

        }
    }

    @Test
    fun `json array with multiple possible patterns`() {

        with(assertionBuilder()) {
            json("""["12", "13"] """) matches validator(
                listOf(
                    "${jsonArrayOf(stringPattern)}",
                    "${jsonArrayOf(numberPattern)}"
                )
            )

            json(
                """
                       [12, 13] 
                    """
            ) matches validator(listOf("${jsonArrayOf(stringPattern)}", "${jsonArrayOf(numberPattern)}"))

        }
    }

    @Test
    fun `json array with multiple possible patterns observed is JsonArray`() {
        with(assertionBuilder()) {

            json(
                mutableListOf("12", "13")
            ) matches validator(listOf("${jsonArrayOf(stringPattern)}", "${jsonArrayOf(numberPattern)}"))

            json(mutableListOf(12, 13)) matches validator(
                listOf(
                    "${jsonArrayOf(stringPattern)}", "${jsonArrayOf(numberPattern)}"
                )
            )

        }
    }

    @Test
    fun `json int does not match when string requested`() {
        with(assertionBuilder()) {

            val exception1 = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "string": 12
                           } 
                        """
                ) matches validator {
                    """
                   {
                        "string": "$stringPattern"
                   } 
                """
                }
            }

            assertEquals("""expected class kotlin.String, got 12 at "string"""", exception1.message)

            val exception2 = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "string": 12
                           } 
                        """
                ) matches validator {
                    """
                   {
                        "string": "hello"
                   } 
                """
                }
            }

            assertEquals("""Expected hello, got 12 at "string"""", exception2.message)

        }
    }

    @Test
    fun `json object with nullable string passes`() {
        with(assertionBuilder()) {

            json(
                """
                       {
                            "string": null
                       } 
                    """
            ) matches validator {
                """
                   {
                        "string": "${stringPattern.nullable}"
                   } 
                """
            }
        }
    }

    @Test
    fun `json object with not nullable number fails`() {
        with(assertionBuilder()) {


            val exception = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "number": null
                           } 
                        """
                ) matches validator {
                    """
                   {
                        "number": "$numberPattern"
                   } 
                """
                }
            }
            assertEquals("""expected none nullable value $numberPattern at "number"""", exception.message)

        }
    }

    @Test
    fun `json object with nullable number passes`() {
        with(assertionBuilder()) {

            json(
                """
                       {
                            "number": null
                       } 
                    """
            ) matches validator {
                """
                   {
                        "number": "${numberPattern.nullable}"
                   } 
                """
            }
        }
    }

    @Test
    fun `json object with not nullable boolean fails`() {
        with(assertionBuilder()) {

            val exception = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "boolean": null
                           } 
                        """
                ) matches validator {
                    """
                   {
                        "boolean": "$booleanPattern"
                   } 
                """
                }
            }
            assertEquals(
                """expected none nullable value $booleanPattern at "boolean"""", exception.message
            )

        }
    }

    @Test
    fun `json object with nullable boolean passes`() {

        with(assertionBuilder()) {

            json(
                """
                       {
                            "boolean": null
                       } 
                    """
            ) matches validator {
                """
                   {
                        "boolean": "${booleanPattern.nullable}"
                   } 
                """
            }
        }
    }

    @Test
    fun `json object with number type not matching`() {
        with(assertionBuilder()) {


            val exception = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "number": "1"
                           } 
                        """
                ) matches validator {
                    """
                   {
                        "number": "$numberPattern"
                   } 
                """
                }
            }
            assertEquals(
                """expected object of type class kotlin.Number, got "1" at "number"""",
                exception.message
            )

        }
    }

    @Test
    fun `json object with boolean type not matching`() {
        with(assertionBuilder()) {


            val exception = assertThrows<AssertionFailedError> {

                json(
                    """
                       {
                            "boolean": "true"
                       } 
                    """
                ) matches validator {
                    """
                        {
                            "boolean": "$booleanPattern"
                        } 
                    """
                }
            }
            assertEquals(
                """expected object of type class kotlin.Boolean, got "true" at "boolean"""",
                exception.message
            )

        }
    }

    @Test
    fun `json object with value not matching`() {
        with(assertionBuilder()) {

            val exception = assertThrows<AssertionFailedError> {
                json(
                    """
                   {
                        "data": "5678"
                   } 
                """
                ) matches validator {
                    """
                    {
                        "data": "1234"
                    } 
                """
                }
            }
            assertEquals("""Expected 1234, got 5678 at "data"""", exception.message)

        }
    }

    @Test
    fun `error path is correctly set on assertion failure message`() {

        with(assertionBuilder()) {

            val exception = assertThrows<AssertionFailedError> {

                json(
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
                ) matches validator {
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
                    """
                }
            }
            assertEquals(
                """Expected level112314, got error here at "level0" > "level11" > "level112[1]" > "level11231" > "level112314"""",
                exception.message
            )

        }
    }

    @Test
    fun `json object with key not matching`() {
        with(assertionBuilder()) {

            val exception = assertThrows<AssertionFailedError> {

                json(
                    """
                               {
                                    "data2": "1234"
                               } 
                            """
                ) matches validator {
                    """
                        {
                            "data1": "1234"
                        } 
                    """
                }
            }
            assertEquals("expected [data1] entries, got [data2] entries at ROOT", exception.message)

        }
    }

    @Test
    fun `json array of simple types strings`() {
        with(assertionBuilder()) {

            json(
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
            ) matches validator {
                """
                    [[{
                        "string": "${stringPattern.nullable}",
                        "number": "${numberPattern.nullable}",
                        "boolean": "${booleanPattern.nullable}",
                        "astring": "hello",
                        "anumber": 1,
                        "aboolean": false
                   }]] 
                """
            }
        }
    }

    @Test
    fun `json array of object in subtype`() {
        with(assertionBuilder()) {

            val descDataPattern = pattern("descdata") definedBy """{"data": "$numberPattern"}"""

            json(
                """
                            {
                                "string": "hello",
                                "array": [
                                    {"data": 1},
                                    {"data": 2}
                                ]
                            }
                    """
            ) matches validator {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": "${jsonArrayOf(descDataPattern)}"
                   } 
                """
            }
        }
    }

    @Test
    fun `json array of nullable object in subtype`() {
        with(assertionBuilder()) {

            val descDataPattern = pattern("descdata") definedBy """{"data": "$numberPattern"}"""

            json(
                """
                            {
                                "string": "hello",
                                "array": [
                                    {"data": 1},
                                    null
                                ]
                            }
                    """
            ) matches validator {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": "${jsonArrayOf(descDataPattern.nullable)}"
                   } 
                """
            }
        }
    }

    @Test
    fun `json nullable array of nullable objects in subtypes - array null`() {
        with(assertionBuilder()) {

            val descDataPattern = pattern("descdata") definedBy """{"data": "$numberPattern"}"""

            json(
                """
                            {
                                "string": "hello",
                                "array": null
                            }
                    """
            ) matches validator {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": "${jsonArrayOf(descDataPattern.nullable).nullable}"
                   } 
                """
            }
        }
    }

    @Test
    fun `json nullable array of nullable objects in subtypes - objects null`() {
        with(assertionBuilder()) {

            val descDataPattern = pattern("descdata") definedBy """{"data": "$numberPattern"}"""

            json(
                """
                            {
                                "string": "hello",
                                "array": [null, null]
                            }
                    """
            ) matches validator {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": "${jsonArrayOf(descDataPattern.nullable).nullable}"
                   } 
                """
            }
        }
    }

    @Test
    fun `array pattern or array of patterns`() {
        with(assertionBuilder()) {

            val dataPattern = pattern("data") definedBy """{"data": "$stringPattern"}"""

            json(
                """
                            {
                                "string": "hello",
                                "array": [{"data": "hello"}, {"data": "world"}]
                            }
                    """
            ) matches validator {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": ["$dataPattern", "$dataPattern"]
                   } 
                """
            }

            json(
                """
                            {
                                "string": "hello",
                                "array": [{"data": "hello"}, {"data": "world"}]
                            }
                    """
            ) matches validator {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": ["$dataPattern", {"data": "world"}]
                   } 
                """
            }

            json(
                """
                            {
                                "string": "hello",
                                "array": [{"data": "hello"}, {"data": "world"}]
                            }
                    """
            ) matches validator(
                checkArraysOrder = false
            ) {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": [{"data": "world"}, "$dataPattern"]
                   } 
                """
            }

            json(
                """
                            {
                                "string": "hello",
                                "array": [{"data": "hello"}, {"data": "world"}]
                            }
                    """
            ) matches validator {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "array": "${jsonArrayOf(dataPattern)}"
                   } 
                """
            }
        }
    }

    @Test
    fun `json array patterns error`() {
        with(assertionBuilder()) {

            val exception = assertThrows<IllegalStateException> {

                json(
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
                ) matches validator {
                    """
                [[
                {
                    "string": "${stringPattern.nullable}"
                },
                {
                    "number": "$numberPattern"
                }
               ]]
                """
                }
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
    }

    @Test
    fun `json matcher of type function`() {
        with(assertionBuilder()) {

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

            json(
                """
                            {
                                "string": "hello",
                                "date": "2021-01-12",
                                "date_array": ["2021-01-12", "2021-01-13"]
                            }
                        """
            ) matches validator {
                """
                        {
                            "string": "${stringPattern.nullable}",
                            "date": "$datePattern",
                            "date_array": "${jsonArrayOf(datePattern)}"
                       } 
                    """
            }

            val exception = assertThrows<AssertionFailedError> {
                json(
                    """
                                {
                                    "string": "hello",
                                    "date": "hello"
                                }
                        """
                ) matches validator {
                    """
                    {
                        "string": "${stringPattern.nullable}",
                        "date": "$datePattern"
                   } 
                """
                }
            }
            assertEquals(""""hello" does not validate pattern $datePattern at "date"""", exception.message)

        }
    }

    @Test
    fun `json matcher of type function - nullable`() {
        with(assertionBuilder()) {

            val datePattern = pattern("date") definedBy { data: String ->
                val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

                try {
                    dateFormatter.parse(data)
                    true
                } catch (e: DateTimeParseException) {
                    false
                }
            }

            json(
                """
                            {
                                "string": "hello",
                                "date": null
                            }
                    """
            ) matches validator {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "date": "${datePattern.nullable}"
                   } 
                """
            }
        }
    }

    @Test
    fun `json matcher of type function - not nullable`() {
        with(assertionBuilder()) {

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

                json(
                    """
                                {
                                    "string": "hello",
                                    "date": null
                                }
                        """
                ) matches validator {
                    """
                    {
                        "string": "${stringPattern.nullable}",
                        "date": "$datePattern"
                   } 
                """
                }
            }
            assertEquals("""expected none nullable value $datePattern at "date"""", exception.message)

        }
    }

    @Test
    fun `json matcher of type function - array`() {
        with(assertionBuilder()) {

            val datePattern = pattern("date") definedBy { data: String ->
                val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

                try {
                    dateFormatter.parse(data)
                    true
                } catch (e: DateTimeParseException) {
                    false
                }
            }

            json(
                """
                            {
                                "string": "hello",
                                "date": ["2012-09-13", "2001-08-24"]
                            }
                    """
            ) matches validator {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern)}"
                   }
                """
            }

            val exception = assertThrows<AssertionFailedError> {
                json(
                    """
                                {
                                    "string": "hello",
                                    "date": ["2012-09-13", "bad format"]
                                }
                        """
                ) matches validator {
                    """
                    {
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern)}"
                   } 
                """
                }
            }
            assertEquals(
                """"bad format" does not validate pattern $datePattern at "date[1]"""",
                exception.message
            )
        }
    }

    @Test
    fun `json matcher of type function - array not nullable`() {
        with(assertionBuilder()) {

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


                json(
                    """
                                {
                                    "string": "hello",
                                    "date": null
                                }
                        """
                ) matches validator {
                    """
                    {
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern)}"
                   }
                """
                }
            }
            assertEquals("""expected none nullable value $datePattern at "date"""", exception1.message)


            val exception2 = assertThrows<AssertionFailedError> {

                json(
                    """
                                {
                                    "string": "hello",
                                    "date": ["2020-12-13", null]
                                }
                        """
                ) matches validator {
                    """
                    {
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern)}"
                   }
                """
                }
            }
            assertEquals("""expected none nullable value $datePattern at "date[1]"""", exception2.message)
        }
    }

    @Test
    fun `json matcher of type function - nullable array`() {
        with(assertionBuilder()) {

            val datePattern = pattern("date") definedBy { data: String ->
                val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

                try {
                    dateFormatter.parse(data)
                    true
                } catch (e: DateTimeParseException) {
                    false
                }
            }

            json(
                """
                            {
                                "string": "hello",
                                "date": null
                            }
                    """
            ) matches validator {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern).nullable}"
                   } 
                """
            }
        }
    }

    @Test
    fun `json matcher of type function - nullable array of nullable elements`() {
        with(assertionBuilder()) {

            val datePattern = pattern("date") definedBy { data: String ->
                val dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd").withResolverStyle(STRICT)

                try {
                    dateFormatter.parse(data)
                    true
                } catch (e: DateTimeParseException) {
                    false
                }
            }

            json(
                """
                            {
                                "string": "hello",
                                "date": ["2014-07-23", null]
                            }
                    """
            ) matches validator {
                """
                    {
                        "string": "${stringPattern.nullable}",
                        "date": "${jsonArrayOf(datePattern.nullable).nullable}"
                   } 
                """
            }
        }
    }

    @Test
    fun `matcher registration - observed is string`() {
        with(assertionBuilder()) {

            val myDataPattern = pattern("mydata") definedBy TestDataObject::class

            json(
                """
                       {
                                "string": "hello",
                                "number": 1,
                                "boolean": false
                           } 
                    """
            ) matches myDataPattern
        }
    }


    @Test
    fun `matcher registration - matcher in subtype`() {
        with(assertionBuilder()) {

            val myDataPattern = pattern("mydata") definedBy TestDataObject::class

            json(
                """
                       {
                            "data": {
                                "string": "hello",
                                "number": 1,
                                "boolean": false
                            }
                       } 
                    """
            ) matches validator {
                """
                   {
                        "data": "$myDataPattern"
                   } 
                """
            }
        }
    }

    @Test
    fun `matcher registration - nullable matcher in subtype`() {
        with(assertionBuilder()) {

            val myDataPattern = pattern("mydata") definedBy TestDataObject::class

            json(
                """
                       {
                            "data": null
                       } 
                    """
            ) matches validator {
                """
                   {
                        "data": "${myDataPattern.nullable}"
                   } 
                """
            }
        }
    }

    @Test
    fun `matcher registration - observed is JsonMap`() {

        with(assertionBuilder()) {
            val myDataPattern = pattern("mydata") definedBy TestDataObject::class

            json(
                JsonMap().apply {
                    put("string", "hello")
                    put("number", 1)
                    put("boolean", false)
                }
            ) matches myDataPattern
        }
    }

    @Test
    fun `multiple possible patterns - observed is JsonMap`() {
        with(assertionBuilder()) {

            json(
                JsonMap().apply {
                    put("string", "hello")
                    put("number", 1)
                    put("boolean", false)
                }
            ) matches validator(
                listOf(
                    """
                       {"hello": "world"}
                    """,
                    """
                       {"string": "$stringPattern", "number": "$numberPattern", "boolean": "$booleanPattern"} 
                    """
                )
            )
        }
    }

    @Test
    fun `matcher registration - observed an array displayed as string`() {
        with(assertionBuilder()) {

            val myDataPattern = pattern("mydata") definedBy TestDataObject::class

            json(
                """
                    [{
                        "string": "hello",
                        "number": 1,
                        "boolean": false
                    }] 
                """
            ) matches jsonArrayOf(myDataPattern)
        }
    }

    @Test
    fun `matcher registration - observed an array displayed as JsonArray`() {
        with(assertionBuilder()) {

            val myDataPattern = pattern("mydata") definedBy TestDataObject::class

            json(
                JsonArray().apply {
                    add(JsonMap().apply {
                        put("string", "hello")
                        put("number", 1)
                        put("boolean", false)
                    })
                }
            ) matches jsonArrayOf(myDataPattern)
        }
    }

    @Test
    fun `matcher registration - matcher in subtype as array`() {
        with(assertionBuilder()) {

            val myDataPattern = pattern("mydata") definedBy TestDataObject::class

            json(
                """
                    {
                        "data": [{
                            "string": "hello",
                            "number": 1,
                            "boolean": false
                        }]
                   } 
                """
            ) matches validator {
                """
                {
                    "data": "${jsonArrayOf(myDataPattern)}"
                } 
            """
            }
        }
    }

    @Test
    fun `matcher registration - nullable matcher in subtype as array`() {
        with(assertionBuilder()) {

            val myDataPattern = pattern("mydata") definedBy TestDataObject::class

            json(
                """
                       {
                            "data": [{
                                "string": "hello",
                                "number": 1,
                                "boolean": false
                            }, null]
                       } 
                    """
            ) matches validator {
                """
                   {
                        "data": "${jsonArrayOf(myDataPattern.nullable)}"
                   } 
                """
            }
        }
    }

    @Test
    fun `matcher registration - observed is JsonArray`() {
        with(assertionBuilder()) {

            val myDataPattern = pattern("mydata") definedBy TestDataObject::class

            json(
                JsonArray().apply {
                    add(
                        JsonMap().apply {
                            put("string", "hello")
                            put("number", 1)
                            put("boolean", false)
                        }
                    )
                }
            ) matches jsonArrayOf(myDataPattern)
        }
    }

    @Test
    fun `matcher registration as class - observed is a polymorphic JsonArray`() {
        with(assertionBuilder()) {

            val yoloPattern = pattern("yolo") definedBy Yolo::class

            json(
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
            ) matches jsonArrayOf(yoloPattern)
        }
    }

    @Test
    fun `matcher registration as string - observed is a polymorphic JsonArray`() {
        with(assertionBuilder()) {

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

            json(
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
            ) matches jsonArrayOf(yoloPattern)
        }
    }

    @Test
    fun `matcher registration as class - observed is a polymorphic json array as string`() {
        with(assertionBuilder()) {

            val yoloPattern = pattern("yolo") definedBy Yolo::class

            json(
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
            ) matches jsonArrayOf(yoloPattern)
        }
    }

    @Test
    fun `matcher registration as string - observed is a polymorphic json array as string`() {
        with(assertionBuilder()) {

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

            json(
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
            ) matches jsonArrayOf(yoloPattern)
        }
    }

    @Test
    fun `matcher registration - polymorphism in arrays`() {
        with(assertionBuilder()) {

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

            json(
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
            ) matches validator {
                """
                   {
                        "data": "${jsonArrayOf(yoloPattern.nullable)}"
                   } 
                """
            }
        }
    }

    @Test
    fun `when check array order is disabled, check must go fast enough`() {
        with(assertionBuilder()) {
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
                json(data1) matches validator(false) { data1 }
            }
            Assertions.assertTrue(time < 1000)
        }
    }

    @Test
    fun `keys may be absent from JSON`() {
        with(assertionBuilder()) {
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

            json(observedPresent) matches validator(ignoreUnknownProperties = true) { expected }
            json(observedPresent) matches validator(ignoreUnknownProperties = false) { expected }
            json(observedAbsent) matches validator(ignoreUnknownProperties = true) { expected }
            json(observedAbsent) matches validator(ignoreUnknownProperties = false) { expected }
        }
    }

    @Test
    fun `sort attributes when expeted pattern and observed data do not have same attributes`() {
        with(assertionBuilder()) {

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
                json(observed) matches validator { expected }
            }

            assertEquals(
                "expected [attr0, attr1, attr2, attr3, attr4, attr5, attr6, attr7, attr8, attr9] entries, got [attr1, attr2, attr3, attr4, attr5, attr7, attr8, attr9, attra, attrb, attrc] entries at ROOT",
                exception.message
            )
        }
    }

    @Test
    fun `optional json key are not in expected error message when not in observed`() {
        with(assertionBuilder()) {

            val exception = assertThrows<AssertionFailedError> {

                json(
                    """
                           {
                                "optKey2": "12",
                                "manKey1": "12"
                           } 
                        """
                ) matches validator {
                    """
                       {
                            ${optionalJsonKey("optKey1")}: "$stringPattern",
                            "manKey2": "$stringPattern",
                            "manKey1": "$stringPattern",
                            ${optionalJsonKey("optKey2")}: "$stringPattern"
                       } 
                    """
                }
            }

            assertEquals(
                """expected [manKey1, manKey2, optKey2, optional(optKey1)] entries, got [manKey1, optKey2] entries at ROOT""",
                exception.message
            )
        }
    }

    @Test
    fun `unclear error message when validating a pattern on an element of an array when pattern is not well-formed json`() {
        with(assertionBuilder()) {

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
                json(observed) matches validator(expected)
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
