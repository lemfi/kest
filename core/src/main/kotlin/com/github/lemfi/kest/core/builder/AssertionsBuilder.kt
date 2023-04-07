@file:Suppress("unused")

package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.FilteredAssertionFailedError
import com.github.lemfi.kest.core.model.IStepName

class AssertionsBuilder(val scenarioName: String, val stepName: IStepName?) {
    operator fun Any?.invoke(message: (() -> String)?) = Explain(this, message)
    data class Explain(val expected: Any?, val message: (() -> String)?)

    infix fun Any?.isEqualTo(expected: Any?) =
        if (expected is Explain) {
            isEqualTo(this, expected.expected, expected.message)
        } else {
            isEqualTo(this, expected)
        }

    fun Any?.isEqualTo(expected: Any?, message: (() -> String)? = null) =
        isEqualTo(this, expected, message)

    infix fun Any?.isNotEqualTo(expected: Any?) =
        if (expected is Explain) {
            isNotEqualTo(this, expected.expected, expected.message)
        } else {
            isNotEqualTo(this, expected)
        }

    val Any?.isTrue: Unit get() = this isTrue null
    val Any?.isFalse: Unit get() = this isFalse null

    @Suppress("unused")
    infix fun Any?.isTrue(message: (() -> String)?) {

        (this ?: false).let { success ->
            if (success !is Boolean || !success) throw FilteredAssertionFailedError(
                message?.invoke() ?: "Expected true, was $this",
                true,
                this
            )
        }
    }

    @Suppress("unused")
    infix fun Any?.isFalse(message: (() -> String)?) {

        (this ?: true).let { failure ->
            if (failure !is Boolean || failure) throw FilteredAssertionFailedError(
                message?.invoke() ?: "Expected false, was $this",
                false,
                this
            )
        }
    }

    @Deprecated(
        "observed.isEqualTo(expected, message) instead",
        replaceWith = ReplaceWith("observed.isEqualTo(expected, message)")
    )
    fun eq(expected: Any?, observed: Any?, message: (() -> String)?) {
        observed isEqualTo expected(message)
    }

    @Deprecated("observed.isTrue(message) instead", replaceWith = ReplaceWith("observed.isTrue(message)"))
    @Suppress("unused", "FunctionName")
    fun `is true`(observed: Any?, message: (() -> String)) {
        observed isTrue message
    }

    @Deprecated("observed.isFalse(message) instead", replaceWith = ReplaceWith("observed.isFalse(message)"))
    @Suppress("unused", "FunctionName")
    fun `is false`(observed: Any?, message: (() -> String)?) {
        observed isFalse message
    }

    @Deprecated("expected this.isTrue instead", replaceWith = ReplaceWith("this.isTrue"))
    val Any?.`is true`: Unit
        get() {
            this isTrue null
        }

    @Deprecated("expected this.isFalse instead", replaceWith = ReplaceWith("this.isFalse"))
    val Any?.`is false`: Unit
        get() {
            this isFalse null
        }

    @Deprecated("observed.isTrue instead", replaceWith = ReplaceWith("observed.isTrue"))
    @Suppress("unused", "FunctionName")
    fun `is true`(observed: Any?) {
        observed isTrue null
    }

    @Deprecated("observed.isFalse instead", replaceWith = ReplaceWith("observed.isFalse"))
    @Suppress("unused", "FunctionName")
    fun `is false`(observed: Any?) {
        observed isFalse null
    }

}

private infix fun Any?.isEqualTo(expected: Any?) = (this?.equals(expected) ?: (expected == null))
private infix fun Any?.isNotEqualTo(expected: Any?) = !(this isEqualTo expected)

private fun isEqualTo(observed: Any?, expected: Any?, message: (() -> String)? = null) {

    (observed isEqualTo expected).let { success ->
        if (!success) throw FilteredAssertionFailedError(
            message?.invoke() ?: "Expected $expected, got $observed",
            expected,
            observed
        )
    }
}

private fun isNotEqualTo(expected: Any?, observed: Any?, message: (() -> String)? = null) {

    (observed isNotEqualTo expected).let { success ->
        if (!success) throw FilteredAssertionFailedError(
            message?.invoke() ?: "Values should be different",
            expected,
            observed
        )
    }
}