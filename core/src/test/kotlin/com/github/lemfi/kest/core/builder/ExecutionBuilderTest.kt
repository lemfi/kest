package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.IStepPostExecution
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ExecutionBuilderTest {


    @Test
    fun `depends on tries to resolve step result`() {

        val executionBuilder = object : ExecutionBuilder<String> {
            override fun toExecution(): Execution<String> {
                throw IllegalAccessError("this function is useless for test")
            }
        }
        
        val stepResult = mockk<IStepPostExecution<String, String>>()

        every { stepResult() } returns "hello world!"

        executionBuilder.apply {
            depends on stepResult
        }

        verify(exactly = 1) { stepResult() }
    }
}