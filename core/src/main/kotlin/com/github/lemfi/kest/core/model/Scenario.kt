package com.github.lemfi.kest.core.model

class Scenario<T>(
        val name: String,
        val steps: MutableList<Step<*>>,
        val result: (()->T)?
)