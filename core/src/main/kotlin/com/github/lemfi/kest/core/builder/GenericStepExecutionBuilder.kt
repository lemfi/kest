package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.IStepPostExecution

class GenericStepBuilder {

    infix fun depends.on(stepResult: IStepPostExecution<*, *>) {
        stepResult()
    }
}