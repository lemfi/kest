package com.github.lemfi.kest.core.builder

import com.github.lemfi.kest.core.model.ScenarioName
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class StandaloneScenarioExecutionBuilderTest {

    @Test
    fun `StandaloneScenarioExecutionBuilder builds a StandaloneScenario`() {
        val scenarioBuilder =
            StandaloneScenarioBuilder()
                .apply {
                    steps.add(
                        StandaloneStep<String>(
                            name = StepName("step 1 of nested scenario"),
                            retry = null,
                            scenarioName = ScenarioName("a scenario name")
                        )
                    )
                    steps.add(
                        StandaloneStep<String>(
                            name = StepName("step 2 of nested scenario"),
                            retry = null,
                            scenarioName = ScenarioName("a scenario name")
                        )
                    )
                }

        scenarioBuilder.name { "hello world" }

        val scenario = scenarioBuilder.toScenario()

        Assertions.assertEquals("hello world", scenario.name.value)
        Assertions.assertEquals(2, scenario.steps.size)
        Assertions.assertEquals("step 1 of nested scenario", scenario.steps[0].name!!.value)
        Assertions.assertEquals("step 2 of nested scenario", scenario.steps[1].name!!.value)
    }
}