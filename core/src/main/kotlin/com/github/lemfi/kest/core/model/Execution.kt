package com.github.lemfi.kest.core.model

@JvmInline
value class ExecutionDescription(val description: String)

abstract class Execution<T> {

    abstract val description: ExecutionDescription?

    abstract fun execute(): T
}