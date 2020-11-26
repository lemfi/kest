package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.Execution

abstract class ExecutionBuilder<T> {

    abstract fun build(): Execution<T>
}