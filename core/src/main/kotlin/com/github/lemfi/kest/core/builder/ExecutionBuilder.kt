package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.Execution

interface ExecutionBuilder<T> {

    fun toExecution(): Execution<T>
}