package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ScenarioBuilderTest {

    @Test
    fun `name is set`() {

        val scenarioBuilder: ScenarioBuilder = StandaloneScenarioBuilder("hello world")

        Assertions.assertEquals("hello world", scenarioBuilder.scenarioName)
    }

    @Test
    fun `a step is added to scenario`() {

        class MyExecutionBuilder : ExecutionBuilder<String> {

            lateinit var hello: String

            override fun toExecution(): Execution<String> = object : Execution<String>() {
                override fun execute(): String = hello
            }
        }

        val myExecutionBuilder = MyExecutionBuilder()
        val scenarioBuilder = StandaloneScenarioBuilder("scenario name")
        val step = StandaloneStep<String>(
            scenarioName = "scenario name",
            name = StepName("step name"),
            retry = null,
        )
        val res = scenarioBuilder.run {
            step.run {
                addToScenario(
                    executionBuilder = myExecutionBuilder,
                    executionConfiguration = { hello = "world" }
                )
            }
        }

        Assertions.assertEquals(1, scenarioBuilder.steps.size)
        Assertions.assertEquals(step, scenarioBuilder.steps[0])
        Assertions.assertEquals("world", step.execution().execute())
        Assertions.assertEquals(step.postExecution, res)
    }

    @Test
    fun `a nested scenario is added to scenario`() {

        class MyExecutionBuilder : ExecutionBuilder<String> {

            lateinit var hello: String

            override fun toExecution(): Execution<String> = object : Execution<String>() {
                override fun execute(): String = hello
            }
        }

        val myExecutionBuilder = MyExecutionBuilder()
        val scenarioBuilder = StandaloneScenarioBuilder("scenario name")
        val step = NestedScenarioStep<String>(
            scenarioName = "scenario name",
            name = StepName("step name"),
            retry = null,
        )
        val res = scenarioBuilder.run {
            step.run {
                addToScenario(
                    executionBuilder = myExecutionBuilder,
                    executionConfiguration = { hello = "world" }
                )
            }
        }

        Assertions.assertEquals(1, scenarioBuilder.steps.size)
        Assertions.assertEquals(step, scenarioBuilder.steps[0])
        Assertions.assertEquals("world", step.execution().execute())
        Assertions.assertEquals(step.postExecution, res)
    }
}