@file:Suppress("ObjectPropertyName", "unused")

package com.github.lemfi.kest.core.cli

import org.opentest4j.AssertionFailedError

fun eq(expected: Any?, observed: Any?, message: (() -> String)? = null) {

    (observed?.equals(expected) ?: (expected == null)).let { success ->
        if (!success) throw AssertionFailedError(
            message?.invoke() ?: "Expected $expected, got $observed",
            expected,
            observed
        )
    }
}

infix fun Any?.eq(observed: Any?) = eq(this, observed)

val Any?.`is true`: Unit get() = `is true`(this)
val Any?.`is false`: Unit get() = `is false`(this)

@Suppress("unused", "FunctionName")
fun `is true`(observed: Any?, message: (() -> String)? = null) {

    (observed ?: false).let { success ->
        if (success !is Boolean || !success) throw AssertionFailedError(
            message?.invoke() ?: "Expected true, was $observed",
            true,
            observed
        )
    }
}

@Suppress("unused", "FunctionName")
fun `is false`(observed: Any?, message: (() -> String)? = null) {

    (observed ?: true).let { failure ->
        if (failure !is Boolean || failure) throw AssertionFailedError(
            message?.invoke() ?: "Expected false, was $observed",
            false,
            observed
        )
    }
}