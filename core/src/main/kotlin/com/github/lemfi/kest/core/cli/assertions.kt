package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import org.opentest4j.AssertionFailedError

fun AssertionsBuilder.eq(expected: Any?, observed: Any?) {

    (observed?.equals(expected) ?: (expected == null)).let { success ->
        if (!success) throw AssertionFailedError("Expected $expected, got $observed", expected, observed)
    }
}

fun AssertionsBuilder.`true`(observed: Boolean?) {

    (observed ?: false).let { success ->
        if (!success) throw AssertionFailedError("Expected true, was $observed", true, observed)
    }
}

fun AssertionsBuilder.`false`(observed: Boolean?) {

    (observed ?: true).let { failure ->
        if (failure) throw AssertionFailedError("Expected false, was $observed", false, observed)
    }
}