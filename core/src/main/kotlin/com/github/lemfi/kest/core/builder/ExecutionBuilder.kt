package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.IStepPostExecution

interface ExecutionBuilder<T> {

    fun toExecution(): Execution<T>

    infix fun depends.on(stepResult: IStepPostExecution<*, *>) {
        stepResult()
    }

}

@Suppress("ClassName")
object depends

