package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import org.opentest4j.AssertionFailedError

fun AssertionsBuilder.eq(expected: Any?, observed: Any?, message: (()->String)? = null) {

    (observed?.equals(expected) ?: (expected == null)).let { success ->
        if (!success) fail(message?.invoke() ?: "Expected $expected, got $observed", expected, observed)
    }
}

@Suppress("unused")
fun AssertionsBuilder.`true`(observed: Boolean?, message: (()->String)? = null) {

    (observed ?: false).let { success ->
        if (!success) fail(message?.invoke() ?: "Expected true, was $observed)", true, observed)
    }
}

@Suppress("unused")
fun AssertionsBuilder.`false`(observed: Boolean?, message: (()->String)? = null) {

    (observed ?: true).let { failure ->
        if (failure) fail(message?.invoke() ?: "Expected false, was $observed", false, observed)
    }
}

fun AssertionsBuilder.fail(message: String, expected: Any?, observed: Any?) {
    val scenario = "Scenario: ${scenarioName.value}"
    val step = if (stepName != null) "Step: ${stepName.value}" else ""
    val max = listOf(scenario, step, message).maxByOrNull { it.length }!!
    throw AssertionFailedError("""
        
        +${(0..max.length + 1).joinToString("") { "-" }}+
        | ${scenario.padEnd(max.length, ' ')} |
        | ${step.padEnd(max.length, ' ')} |
        |${(0..max.length + 1).joinToString("") { " " }}|
        | ${message.padEnd(max.length, ' ')} |
        +${(0..max.length + 1).joinToString("") { "-" }}+
    """.trimIndent(), expected, observed)
}

fun AssertionsBuilder.fail(message: String, cause: Throwable) {
    val scenario = "Scenario: ${scenarioName.value}"
    val step = if (stepName != null) "Step: ${stepName.value}" else ""
    val max = listOf(scenario, step, message).maxByOrNull { it.length }!!
    throw AssertionFailedError("""
        
        +${(0..max.length + 1).joinToString("") { "-" }}+
        | ${scenario.padEnd(max.length, ' ')} |
        | ${step.padEnd(max.length, ' ')} |
        |${(0..max.length + 1).joinToString("") { " " }}|
        | ${message.padEnd(max.length, ' ')} |
        +${(0..max.length + 1).joinToString("") { "-" }}+
    """.trimIndent(), cause)
}