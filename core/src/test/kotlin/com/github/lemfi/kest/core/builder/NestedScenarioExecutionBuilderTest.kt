package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepResultFailure
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NestedScenarioExecutionBuilderTest {

    @Test
    fun `NestedScenarioExecutionBuilder builds a NestedScenario`() {
        val nestedScenarioExecutionBuilder =
            NestedScenarioExecutionBuilder<String>("hello world")
                .apply {
                    step = StandaloneStep(
                        name = StepName("a step"),
                        retry = null,
                        scenarioName = "a scenario name"
                    )
                }
                .apply {
                    steps.add(
                        StandaloneStep<String>(
                            name = StepName("step 1 of nested scenario"),
                            retry = null,
                            scenarioName = "a scenario name"
                        )
                    )
                    steps.add(
                        StandaloneStep<String>(
                            name = StepName("step 2 of nested scenario"),
                            retry = null,
                            scenarioName = "a scenario name"
                        )
                    )
                }

        val scenario = nestedScenarioExecutionBuilder.toScenario()

        Assertions.assertEquals(2, scenario.steps.size)
        Assertions.assertEquals("hello world", scenario.name)
        Assertions.assertEquals("step 1 of nested scenario", scenario.steps[0].name.value)
        Assertions.assertEquals("step 2 of nested scenario", scenario.steps[1].name.value)
    }

    @Test
    fun `NestedScenarioExecutionBuilder builds a NestedScenarioStepExecution`() {
        val step = StandaloneStep<String>(
            name = StepName("a step"),
            retry = null,
            scenarioName = "a scenario name"
        )

        val nestedScenarioExecutionBuilder = NestedScenarioExecutionBuilder<String>("hello world")
            .apply { this.step = step }

        val spy = spyk(nestedScenarioExecutionBuilder)

        val execution = spy.toExecution()

        Assertions.assertTrue(execution is NestedScenarioStepExecution)
        val nestedScenarioStepExecution = execution as NestedScenarioStepExecution

        verify(exactly = 0) { spy.toScenario() }
        nestedScenarioStepExecution.scenario()
        verify(exactly = 1) { spy.toScenario() }

        Assertions.assertEquals(step, execution.step)
    }

    @Test
    fun `a default name is given to scenario`() {
        val nestedScenarioExecutionBuilder =
            NestedScenarioExecutionBuilder<String>(null)
                .apply {
                    step = StandaloneStep(
                        name = StepName("a step"),
                        retry = null,
                        scenarioName = "a scenario name"
                    )
                }

        val scenario = nestedScenarioExecutionBuilder.toScenario()

        Assertions.assertEquals("anonymous nested scenario", scenario.name)
    }

    @Test
    fun `result of scenario is not resolved if returns was not invoked`() {
        val nestedScenarioExecutionBuilder =
            NestedScenarioExecutionBuilder<String>("hello world")
                .apply {
                    step = StandaloneStep(
                        name = StepName("a step"),
                        retry = null,
                        scenarioName = "a scenario name"
                    )
                }

        val exception = assertThrows<StepResultFailure> {
            nestedScenarioExecutionBuilder
                .toScenario()
                .resolve()
        }

        Assertions.assertNotNull(exception.cause)
        Assertions.assertTrue(exception.cause is IllegalArgumentException)
        Assertions.assertEquals("A nested scenario must have a result!", exception.cause!!.message)
    }

    @Test
    fun `result of scenario is resolved`() {
        val nestedScenarioExecutionBuilder =
            NestedScenarioExecutionBuilder<String>("hello world")
                .apply {
                    step = StandaloneStep(
                        name = StepName("a step"),
                        retry = null,
                        scenarioName = "a scenario name"
                    )
                }

        nestedScenarioExecutionBuilder.returns { "this should be returned" }

        Assertions.assertEquals(
            "this should be returned", nestedScenarioExecutionBuilder
                .toScenario()
                .resolve()
        )
    }
}