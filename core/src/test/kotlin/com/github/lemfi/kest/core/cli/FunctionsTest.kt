package com.github.lemfi.kest.core.cli

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.builder.StandaloneScenarioBuilder
import com.github.lemfi.kest.core.model.DefaultStepName
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.IStepName
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.model.StandaloneScenario
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StandaloneStepPostExecution
import com.github.lemfi.kest.core.model.StandaloneStepResult
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepResultFailure
import com.github.lemfi.kest.core.model.`by intervals of`
import com.github.lemfi.kest.core.model.ms
import com.github.lemfi.kest.core.model.times
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkConstructor
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError
import kotlin.system.measureTimeMillis

class FunctionsTest {

    @Test
    fun `build a scenario`() {

        val scenarioBuilder: ScenarioBuilder.() -> Unit = mockk()

        val slot = slot<ScenarioBuilder>()
        every { scenarioBuilder.invoke(capture(slot)) } returns Unit

        mockkConstructor(StandaloneScenarioBuilder::class)
        val scenario = mockk<Scenario>()

        every { anyConstructed<StandaloneScenarioBuilder>().toScenario() } returns scenario

        val res = scenario("a scenario name", scenarioBuilder)

        verify { scenarioBuilder(capture(slot)) }

        Assertions.assertTrue(slot.captured is StandaloneScenarioBuilder)
        assertEquals(scenario, res)

        unmockkConstructor(StandaloneScenarioBuilder::class)

    }

    @Test
    fun `add assertions to result of a step`() {
        val stepResult = mockk<StandaloneStepPostExecution<String, Int, Long>>(relaxUnitFun = true)

        val l = slot<AssertionsBuilder.(stepResult: String) -> Unit>()
        every { stepResult.addAssertion(capture(l)) } returns Unit

        val res = stepResult `assert that` {
            `is false`(true) { "fail message" }
        }

        verify { stepResult.addAssertion(any()) }

        val assertionsResult = assertThrows<AssertionFailedError> {
            l.invoke(mockk(), "step res")
        }

        assertEquals("fail message", assertionsResult.message)
        assertEquals(stepResult, res)
    }

    @Test
    fun `run scenario`() {

        mockkStatic(Step<Any>::run)

        val scenario = mockk<StandaloneScenario>()

        val step1 = mockk<StandaloneStep<String>>()
        val step2 = mockk<StandaloneStep<String>>()

        every { step1.run() } returns mockk()
        every { step2.run() } returns mockk()

        every { scenario.steps } returns mutableListOf(step1, step2)

        scenario.run()

        verify(exactly = 1) { step1.run() }
        verify(exactly = 1) { step2.run() }

        unmockkStatic(Step<Any>::run)
    }

    @Test
    fun `run a step may fail while building execution`() {
        val step = mockk<StandaloneStep<String>>(relaxUnitFun = true)
        val postExecution = mockk<StandaloneStepResult<String>>(relaxUnitFun = true)

        every { step.execution } returns { throw IllegalAccessException("this step will fail on execution build!") }
        every { step.postExecution } returns postExecution

        val exception = assertThrows<IllegalAccessException> {
            step.run()
        }

        verify { postExecution.setFailed(exception) }

        assertEquals("this step will fail on execution build!", exception.message)
    }

    @Test
    fun `run a step may fail on execution`() {
        val step = mockk<StandaloneStep<String>>(relaxUnitFun = true)
        val postExecution = mockk<StandaloneStepResult<String>>(relaxUnitFun = true)

        every { step.retry } returns null
        every { step.scenarioName } returns "the scenario name"
        every { step.name } returns object : IStepName {
            override val value = "the step name"
        }

        var onAssertionFailedCalled = false
        var onExecutionEndedCalled = false

        val execution = object : Execution<String>() {
            override fun execute(): String {
                throw IllegalAccessException("this step will fail on execution!")
            }

            override fun onAssertionSuccess() {
                fail("When a step fails, onAssertionSuccess should not be called")
            }

            override fun onAssertionFailedError() {
                onAssertionFailedCalled = true
            }

            override fun onExecutionEnded() {
                onExecutionEndedCalled = true
            }
        }
        every { step.execution } returns { execution }
        every { step.postExecution } returns postExecution

        val exception = assertThrows<AssertionFailedError> {
            step.run()
        }

        val failing = slot<Throwable>()
        verify(exactly = 1) { postExecution.setFailed(capture(failing)) }

        assertEquals(IllegalAccessException::class, failing.captured::class)
        assertEquals("this step will fail on execution!", failing.captured.message)
        Assertions.assertTrue(onAssertionFailedCalled)
        Assertions.assertTrue(onExecutionEndedCalled)

        assertEquals(
            """

Scenario: the scenario name
    Step: the step name

this step will fail on execution!
""", exception.message
        )
    }

    @Test
    fun `run a step may fail on assertions verifications`() {
        val step =
            StandaloneStep<String>(
                scenarioName = "the scenario name",
                name = StepName("the step name"),
                retry = null
            )
        val standaloneStandaloneStepResult: StandaloneStepResult<String> =
            StandaloneStepResult<String>(step, null) { it }
                .apply {
                    assertions.add { `is true`(false) { "hahaha" } }
                }

        var onAssertionFailedCalled = false
        var onExecutionEndedCalled = false

        step.apply {
            postExecution = standaloneStandaloneStepResult
            execution = {
                object : Execution<String>() {
                    override fun execute(): String = "execution is successful"

                    override fun onAssertionSuccess() {
                        fail("When a step fails, onAssertionSuccess should not be called")
                    }

                    override fun onAssertionFailedError() {
                        onAssertionFailedCalled = true
                    }

                    override fun onExecutionEnded() {
                        onExecutionEndedCalled = true
                    }
                }
            }
        }

        val exception = assertThrows<AssertionFailedError> {
            step.run()
        }

        Assertions.assertTrue(step.postExecution.isFailed())
        Assertions.assertTrue(onAssertionFailedCalled)
        Assertions.assertTrue(onExecutionEndedCalled)

        assertEquals(
            """

Scenario: the scenario name
    Step: the step name

hahaha
""", exception.message
        )
    }

    @Test
    fun `a test may fail once and pass when retried`() {

        val step = mockk<StandaloneStep<String>>(relaxUnitFun = true)
        val postExecution = mockk<StandaloneStepResult<String>>(relaxUnitFun = true)

        every { step.retry } returns 2.times.`by intervals of`(10.ms)
        every { step.scenarioName } returns "the scenario name"
        every { step.name } returns object : IStepName {
            override val value = "the step name"
        }
        val execution = mockk<Execution<String>>(relaxUnitFun = true)
        every { step.execution } returns { execution }
        every { execution.execute() } throws IllegalAccessException("first call fails") andThen "success"
        every { step.postExecution } returns postExecution
        every { postExecution.assertions } returns mutableListOf({ `is true`(true) })

        val res = step.run()

        verify(exactly = 1) { postExecution.setResult("success") }
        verify(exactly = 2) { execution.execute() }
        verify(exactly = 1) { execution.onAssertionFailedError() }
        verify(exactly = 1) { execution.onAssertionSuccess() }
        verify(exactly = 1) { execution.onExecutionEnded() }

        assertEquals(step, res)
    }

    @Test
    fun `a step that depends on another one which failed is not retried`() {

        val firstStep = mockk<StandaloneStep<Unit>>(relaxUnitFun = true)
        every { firstStep.scenarioName } returns "the scenario name"
        every { firstStep.name } returns object : IStepName {
            override val value = "the first step name"
        }
        every { firstStep.postExecution } returns StandaloneStepResult(firstStep, null) {}

        val step = mockk<StandaloneStep<Unit>>(relaxUnitFun = true)
        val postExecution = mockk<StandaloneStepResult<Unit>>(relaxUnitFun = true)

        every { step.retry } returns 2.times.`by intervals of`(10.ms)
        every { step.scenarioName } returns "the scenario name"
        every { step.name } returns object : IStepName {
            override val value = "the step name"
        }
        var executionCalled = 0
        val execution = object : Execution<Unit>() {
            override fun execute() {
                executionCalled++
                firstStep.postExecution()
            }
        }
        every { step.execution } returns { execution }
        every { step.postExecution } returns postExecution
        every { postExecution.assertions } returns mutableListOf({ `is true`(true) })

        assertThrows<StepResultFailure> { step.run() }

        assertEquals(1, executionCalled)

    }

    @Test
    fun `and sometimes a test just goes fine!`() {

        val step = mockk<StandaloneStep<String>>(relaxUnitFun = true)
        val postExecution = mockk<StandaloneStepResult<String>>(relaxUnitFun = true)

        every { step.retry } returns null
        every { step.scenarioName } returns "the scenario name"
        every { step.name } returns object : IStepName {
            override val value = "the step name"
        }
        val execution = mockk<Execution<String>>(relaxUnitFun = true)
        every { step.execution } returns { execution }
        every { execution.execute() } returns "success"
        every { step.postExecution } returns postExecution
        every { postExecution.assertions } returns mutableListOf({ `is true`(true) })

        val res = step.run()

        verify(exactly = 1) { postExecution.setResult("success") }
        verify(exactly = 1) { execution.execute() }
        verify(exactly = 0) { execution.onAssertionFailedError() }
        verify(exactly = 1) { execution.onAssertionSuccess() }
        verify(exactly = 1) { execution.onExecutionEnded() }

        assertEquals(step, res)
    }

    @Test
    fun `wait step just waits`() {

        val scenarioBuilder = mockk<ScenarioBuilder>(relaxUnitFun = true)
        every { scenarioBuilder.scenarioName } returns "a name"

        val waitExecutionBuilder = slot<() -> ExecutionBuilder<Unit>>()
        val stepRes = mockk<StandaloneStepResult<Unit>>()

        every { scenarioBuilder.createStep(any(), any(), capture(waitExecutionBuilder)) } returns stepRes

        val res = with(scenarioBuilder) {
            wait(200)
        }

        val execution = waitExecutionBuilder.captured().toExecution()

        val time = measureTimeMillis { execution.execute() }

        assertEquals(stepRes, res)
        Assertions.assertTrue(time in 200..202) { "wait should take 200ms" }
    }

    @Test
    fun `generic step just executes lambda`() {

        val scenarioBuilder = mockk<ScenarioBuilder>(relaxUnitFun = true)
        every { scenarioBuilder.scenarioName } returns "a name"

        val executionBuilder = slot<() -> ExecutionBuilder<String>>()
        val stepRes = mockk<StandaloneStepResult<String>>()

        every { scenarioBuilder.steps } returns mutableListOf()


        every {
            scenarioBuilder.createStep(
                name = DefaultStepName("generic step"),
                retry = null,
                builder = capture(executionBuilder)
            )
        } returns stepRes


        val res = with(scenarioBuilder) {
            step { "HELLO WORLD!" }
        }

        val execution = executionBuilder.captured()
            .toExecution()

        val executionResult = execution.execute()

        assertEquals(stepRes, res)
        assertEquals("HELLO WORLD!", executionResult)

    }

    @Test
    fun `a step may be a scenario`() {
        val scenarioBuilder = StandaloneScenarioBuilder()

        with(scenarioBuilder) {
            nestedScenario(name = "my nested scenario") {
                wait(200)
            }
        }

        val time = scenarioBuilder.toScenario().run { measureTimeMillis { steps.first().run() } }

        Assertions.assertTrue(time in 200..202) { "nested scenario should take 200ms" }
    }

    @Test
    fun `a step may be a scenario that returns something`() {

        val scenarioBuilder = StandaloneScenarioBuilder()

        with(scenarioBuilder) {
            nestedScenario<String>(name = "my nested scenario") {
                val s = step { "played!" }
                returns { s() }
            }
        }

        val stepExecution = scenarioBuilder.toScenario().run { steps.first().run() }

        assertEquals("played!", stepExecution.postExecution())
    }

}
