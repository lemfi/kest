package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.Execution
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
        val res = scenarioBuilder.createStep(
            name = StepName("my created step"),
            retry = null,
        ) { myExecutionBuilder.apply { hello = "world" } }

        Assertions.assertEquals(1, scenarioBuilder.steps.size)
        Assertions.assertEquals("my created step", scenarioBuilder.steps[0].name.value)
        Assertions.assertEquals("world", scenarioBuilder.steps[0].execution().execute())
        Assertions.assertEquals(scenarioBuilder.steps[0].future, res)
    }

    @Test
    fun `a nested scenario is added to scenario`() {

        val myExecutionBuilder = NestedScenarioExecutionBuilder<String>("a name").apply {
            returns { "world" }
        }
        val scenarioBuilder = StandaloneScenarioBuilder("scenario name")

        val res = scenarioBuilder
            .createNestedScenarioStep { myExecutionBuilder }

        Assertions.assertEquals(1, scenarioBuilder.steps.size)
        Assertions.assertEquals("world", scenarioBuilder.steps[0].execution().execute())
        Assertions.assertEquals(scenarioBuilder.steps[0].future, res)
    }
}