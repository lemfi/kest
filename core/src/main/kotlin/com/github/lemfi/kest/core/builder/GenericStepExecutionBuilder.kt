package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.IStepResult

class GenericStepBuilder {

    infix fun depends.on(stepResult: IStepResult<*, *>) {
        stepResult()
    }
}