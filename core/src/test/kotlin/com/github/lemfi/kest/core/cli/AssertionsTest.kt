package com.github.lemfi.kest.core.cli

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError

class AssertionsTest {

    @Test
    fun `test 'eq'`() {
        eq("a", "a")
        eq(null, null)

        null eq null

        "a" eq "a"

        val ex1 = assertThrows<AssertionFailedError> { eq("a", null) }
        Assertions.assertEquals("Expected a, got null", ex1.message)

        val ex2 = assertThrows<AssertionFailedError> { eq("a", null) { "this is a message" } }
        Assertions.assertEquals("this is a message", ex2.message)

        val ex3 = assertThrows<AssertionFailedError> { "a" eq "b" }
        Assertions.assertEquals("Expected a, got b", ex3.message)

    }

    @Test
    fun `test 'is true'`() {
        `is true`(true)
        true.`is true`

        val ex1 = assertThrows<AssertionFailedError> { `is true`("a") }
        Assertions.assertEquals("Expected true, was a", ex1.message)

        val ex2 = assertThrows<AssertionFailedError> { `is true`(null) }
        Assertions.assertEquals("Expected true, was null", ex2.message)

        val ex3 = assertThrows<AssertionFailedError> { `is true`("a") { "this is a message" } }
        Assertions.assertEquals("this is a message", ex3.message)

        val ex4 = assertThrows<AssertionFailedError> { "a".`is true` }
        Assertions.assertEquals("Expected true, was a", ex4.message)

    }

    @Test
    fun `test 'is false'`() {
        `is false`(false)
        false.`is false`

        val ex1 = assertThrows<AssertionFailedError> { `is false`("a") }
        Assertions.assertEquals("Expected false, was a", ex1.message)

        val ex2 = assertThrows<AssertionFailedError> { `is false`(null) }
        Assertions.assertEquals("Expected false, was null", ex2.message)

        val ex3 = assertThrows<AssertionFailedError> { `is false`("a") { "this is a message" } }
        Assertions.assertEquals("this is a message", ex3.message)

        val ex4 = assertThrows<AssertionFailedError> { "b".`is false` }
        Assertions.assertEquals("Expected false, was b", ex4.message)

    }
}