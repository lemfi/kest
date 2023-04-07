@file:Suppress("ObjectPropertyName", "unused")

package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.model.FilteredAssertionFailedError

@Deprecated("observed.isEqualTo(expected, message) instead", replaceWith = ReplaceWith("observed.isEqualTo(expected, message)"))
fun eq(expected: Any?, observed: Any?, message: (() -> String)?) {

    (observed?.equals(expected) ?: (expected == null)).let { success ->
        if (!success) throw FilteredAssertionFailedError(
            message?.invoke() ?: "Expected $expected, got $observed",
            expected,
            observed
        )
    }
}

@Deprecated("observed.isEqualTo(expected) instead", replaceWith = ReplaceWith("observed.isEqualTo(expected)"))
@JvmName("deprecatedEqNoMessage")
fun eq(expected: Any?, observed: Any?) {
    (observed?.equals(expected) ?: (expected == null)).let { success ->
        if (!success) throw FilteredAssertionFailedError(
            "Expected $expected, got $observed",
            expected,
            observed
        )
    }
}

@Deprecated("expected isEqualTo observed instead", replaceWith = ReplaceWith("this.isEqualTo(expected)"))
infix fun Any?.eq(expected: Any?) {
    (expected?.equals(this) ?: (this == null)).let { success ->
        if (!success) throw FilteredAssertionFailedError(
            "Expected $expected, got $this",
            expected,
            this,
        )
    }
}

@Deprecated("expected this.isTrue instead", replaceWith = ReplaceWith("this.isTrue"))
val Any?.`is true`: Unit get() {
    (this ?: false).let { success ->
        if (success !is Boolean || !success) throw FilteredAssertionFailedError(
            "Expected true, was $this",
            true,
            this
        )
    }
}

@Deprecated("expected this.isFalse instead", replaceWith = ReplaceWith("this.isFalse"))
val Any?.`is false`: Unit get()  {

    (this ?: true).let { failure ->
        if (failure !is Boolean || failure) throw FilteredAssertionFailedError(
            "Expected false, was $this",
            false,
            this
        )
    }
}

@Deprecated("observed.isTrue(message) instead", replaceWith = ReplaceWith("observed.isTrue(message)"))
@Suppress("unused", "FunctionName")
fun `is true`(observed: Any?, message: (() -> String)?) {

    (observed ?: false).let { success ->
        if (success !is Boolean || !success) throw FilteredAssertionFailedError(
            message?.invoke() ?: "Expected true, was $observed",
            true,
            observed
        )
    }
}
@Deprecated("observed.isTrue instead", replaceWith = ReplaceWith("observed.isTrue"))
@Suppress("unused", "FunctionName")
fun `is true`(observed: Any?) {

    (observed ?: false).let { success ->
        if (success !is Boolean || !success) throw FilteredAssertionFailedError(
            "Expected true, was $observed",
            true,
            observed
        )
    }
}

@Deprecated("observed.isFalse(message) instead", replaceWith = ReplaceWith("observed.isFalse(message)"))
@Suppress("unused", "FunctionName")
fun `is false`(observed: Any?, message: (() -> String)? = null) {

    (observed ?: true).let { failure ->
        if (failure !is Boolean || failure) throw FilteredAssertionFailedError(
            message?.invoke() ?: "Expected false, was $observed",
            false,
            observed
        )
    }
}