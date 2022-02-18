package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.core.model.ScenarioName
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ScenarioBuilderTest {

    @Test
    fun `name is set`() {

        val scenarioBuilder: ScenarioBuilder = StandaloneScenarioBuilder()

        scenarioBuilder.name { "hello world" }

        Assertions.assertEquals("hello world", scenarioBuilder.name.value)
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
        val scenarioBuilder = StandaloneScenarioBuilder()
        val step = StandaloneStep<String>(
            scenarioName = ScenarioName("scenario name"),
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
        val scenarioBuilder = StandaloneScenarioBuilder()
        val step = NestedScenarioStep<String>(
            scenarioName = ScenarioName("scenario name"),
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