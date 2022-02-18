package com.github.lemfi.kest.core.executor

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.NestedScenario
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NestedScenarioStepExecutionTest {

    @Test
    fun `nested scenario executor builds the scenario, runs it and then resolves it`() {

        mockkStatic(IScenario::run)

        val scenario = mockk<NestedScenario<String>>()

        val executor = NestedScenarioStepExecution(
            step = mockk(),
            scenario = { scenario }
        )

        every { scenario.steps } returns mutableListOf()
        every { scenario.resolve() } returns "this is the result"

        val res = executor.execute()

        verify(exactly = 1) { scenario.run() }
        verify(exactly = 1) { scenario.resolve() }

        Assertions.assertEquals("this is the result", res)

        unmockkStatic(IScenario::run)
    }

}