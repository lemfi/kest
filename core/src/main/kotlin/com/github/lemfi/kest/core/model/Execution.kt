package com.github.lemfi.kest.core.model

abstract class Execution<T> {

    abstract fun execute(): T
}