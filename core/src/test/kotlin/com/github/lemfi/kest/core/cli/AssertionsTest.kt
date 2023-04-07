package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError

class AssertionsTest {

    val builder = AssertionsBuilder("", null)

    @Test
    fun `test 'eq'`() {

        with(builder) {

            "a" isEqualTo "a"
            null isEqualTo null

            val ex1 = assertThrows<AssertionFailedError> { null isEqualTo "a" }
            Assertions.assertEquals("Expected a, got null", ex1.message)

            val ex2 = assertThrows<AssertionFailedError> { null isEqualTo "a" { "this is a message" } }
            Assertions.assertEquals("this is a message", ex2.message)

            val ex3 = assertThrows<AssertionFailedError> { "a" isEqualTo  "b" }
            Assertions.assertEquals("Expected b, got a", ex3.message)

        }
    }

    @Test
    fun `test 'is true'`() {

        with(builder) {

            true.isTrue

            val ex1 = assertThrows<AssertionFailedError> { "a".isTrue }
            Assertions.assertEquals("Expected true, was a", ex1.message)

            val ex2 = assertThrows<AssertionFailedError> { null.isTrue }
            Assertions.assertEquals("Expected true, was null", ex2.message)

            val ex3 = assertThrows<AssertionFailedError> { "a" isTrue { "this is a message" } }
            Assertions.assertEquals("this is a message", ex3.message)

            val ex4 = assertThrows<AssertionFailedError> { "a".isTrue }
            Assertions.assertEquals("Expected true, was a", ex4.message)

        }
    }

    @Test
    fun `test 'is false'`() {
        with(builder) {
            false.isFalse

            val ex1 = assertThrows<AssertionFailedError> { "a".isFalse }
            Assertions.assertEquals("Expected false, was a", ex1.message)

            val ex2 = assertThrows<AssertionFailedError> { null.isFalse }
            Assertions.assertEquals("Expected false, was null", ex2.message)

            val ex3 = assertThrows<AssertionFailedError> { "a" isFalse { "this is a message" } }
            Assertions.assertEquals("this is a message", ex3.message)

            val ex4 = assertThrows<AssertionFailedError> { "b".isFalse }
            Assertions.assertEquals("Expected false, was b", ex4.message)

        }
    }
}