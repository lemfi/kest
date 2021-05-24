package com.github.lemfi.kest.core.model

abstract class Execution<T> {

    abstract val name: StepName?

    abstract fun execute(): T
}