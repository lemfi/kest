package com.github.lemfi.kest.core.model

abstract class Execution<RESULT> {
    abstract fun execute(): RESULT

    open fun onAssertionFailedError() {}
    open fun onAssertionSuccess() {}
    open fun onExecutionEnded() {}
}