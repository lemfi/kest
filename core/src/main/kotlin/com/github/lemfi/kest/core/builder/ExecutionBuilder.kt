package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.IStepResult

interface ExecutionBuilder<RESULT> {

    fun toExecution(): Execution<RESULT>

    infix fun depends.on(stepResult: IStepResult<*, *>) {
        stepResult()
    }

}

@Suppress("ClassName")
object depends

