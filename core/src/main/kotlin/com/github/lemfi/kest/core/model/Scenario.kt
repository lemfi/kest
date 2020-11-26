package com.github.lemfi.kest.core.model

class Scenario(
        val name: String,
        val steps: MutableList<Step<*>>,
)