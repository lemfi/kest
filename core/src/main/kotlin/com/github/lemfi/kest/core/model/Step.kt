package com.github.lemfi.kest.core.model

import com.github.lemfi.kest.core.builder.AssertionsBuilder

data class Step<T>(
        val execution: Execution<T>,
        var assertions: AssertionsBuilder.(T)->Unit = {},
)