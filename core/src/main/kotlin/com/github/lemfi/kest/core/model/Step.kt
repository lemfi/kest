package com.github.lemfi.kest.core.model

import com.github.lemfi.kest.core.builder.AssertionsBuilder

data class Step<T>(
        val execution: () -> Execution<T>,
        var assertions: MutableList<AssertionsBuilder.(T)->Unit> = mutableListOf(),
        val retry: RetryStep?
)

data class RetryStep(
        val retries: Int = 3,
        val delay: Long = 1000L,
)