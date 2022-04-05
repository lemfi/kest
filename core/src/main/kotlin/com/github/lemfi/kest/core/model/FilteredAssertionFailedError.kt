package com.github.lemfi.kest.core.model

import com.github.lemfi.kest.core.properties.assertionProperty
import org.opentest4j.AssertionFailedError

class FilteredAssertionFailedError(message: String?, expected: Any?, actual: Any?, throwable: Throwable? = null) :
    AssertionFailedError(message, expected, actual, throwable) {
    constructor(message: String?, throwable: Throwable) : this(message, null, null, throwable)
    constructor(message: String?) : this(message, null, null, null)

    init {
        if (assertionProperty { filterStackTraces }) {
            val packageNameToFilter = stackTrace.getOrNull(0)?.className?.substringBeforeLast(".")
            stackTrace = packageNameToFilter?.let {
                stackTrace.mapNotNull {
                    if (it.className.contains(packageNameToFilter)) null else it
                }.toTypedArray()
            } ?: stackTrace
        }
    }
}