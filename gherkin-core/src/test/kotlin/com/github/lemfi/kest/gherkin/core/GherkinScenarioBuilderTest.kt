package com.github.lemfi.kest.gherkin.core

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.core.cli.step
import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.core.model.AssertableStepResult
import com.github.lemfi.kest.core.model.StepName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError

class GherkinScenarioBuilderTest {

    private val builder = GherkinScenarioBuilder(listOf("com.github.lemfi.kest.gherkin.core.tests.stepdefinitions"))

    @Test
    fun `a simple feature with unrelated steps`() {
        val features = builder.resourceToScenarios(
            listOf(
                """Feature: My Test feature

  Scenario: A boring scenario
    Given a step that does something where we do not need the result
    And another step that does something where we do not need the result
    When something happens, but I do not care what
    Then I can check another thing, whatever, that scenario is boring!
"""
            )
        )

        assertEquals(1, features.size)

        val feature = features.first()
        assertEquals("My Test feature", feature.name)

        assertEquals(1, feature.steps.size)
        val scenario = feature.steps.first()

        assertTrue(scenario is NestedScenarioStep)
        scenario as NestedScenarioStep

        val execution = scenario.execution()
        assertTrue(execution is NestedScenarioStepExecution)
        execution as NestedScenarioStepExecution

        val builtScenario = execution.scenario()
        assertEquals("A boring scenario", builtScenario.name)

        assertEquals(4, builtScenario.steps.size)
        val step1 = builtScenario.steps[0]
        val step2 = builtScenario.steps[1]
        val step3 = builtScenario.steps[2]
        val step4 = builtScenario.steps[3]

        assertEquals("Given a step that does something where we do not need the result", step1.name.value)
        assertEquals("And another step that does something where we do not need the result", step2.name.value)
        assertEquals("When something happens, but I do not care what", step3.name.value)
        assertEquals("Then I can check another thing, whatever, that scenario is boring!", step4.name.value)

        assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
        assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
        assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
        assertTrue((step4.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
        assertEquals(1, (step4.future as AssertableStepResult<*, *, *>).assertions.size)

        val assertion = (step4.future as AssertableStepResult<*, *, *>).assertions.first()

        val exception = assertThrows<AssertionFailedError> {
            AssertionsBuilder("", StepName("...")).run {
                @Suppress("UNCHECKED_CAST")
                (assertion as AssertionsBuilder.(String) -> Unit)("hello")
            }
        }

        assertEquals("a boring scenario", exception.expected.value)
    }

    @Test
    fun `a simple feature with step results passed to next steps, ending with an assertion`() {
        val features = builder.resourceToScenarios(
            listOf(
                """Feature: My Test feature

  Scenario: String concatenation
    Given a static string
    And another static string
    When they are concatenated
    Then the result is
"""
            )
        )

        assertEquals(1, features.size)

        val feature = features.first()
        assertEquals("My Test feature", feature.name)

        assertEquals(1, feature.steps.size)
        val scenario = feature.steps.first()

        assertTrue(scenario is NestedScenarioStep)
        scenario as NestedScenarioStep

        val execution = scenario.execution()
        assertTrue(execution is NestedScenarioStepExecution)
        execution as NestedScenarioStepExecution

        val builtScenario = execution.scenario()
        assertEquals("String concatenation", builtScenario.name)

        assertEquals(4, builtScenario.steps.size)
        val step1 = builtScenario.steps[0]
        val step2 = builtScenario.steps[1]
        val step3 = builtScenario.steps[2]
        val step4 = builtScenario.steps[3]

        assertEquals("Given a static string", step1.name.value)
        assertEquals("And another static string", step2.name.value)
        assertEquals("When they are concatenated", step3.name.value)
        assertEquals("Then the result is", step4.name.value)

        assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
        assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
        assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
        assertTrue((step4.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
        assertEquals(1, (step4.future as AssertableStepResult<*, *, *>).assertions.size)

        val assertion = (step4.future as AssertableStepResult<*, *, *>).assertions.first()

        val exception = assertThrows<AssertionFailedError> {
            AssertionsBuilder("", StepName("...")).run {
                @Suppress("UNCHECKED_CAST")
                (assertion as AssertionsBuilder.(String) -> Unit)("hello")
            }
        }

        assertEquals("a static stringanother static string", exception.expected.value)
    }

    @Test
    fun `two distinct features generate two scenarios`() {
        val features = builder.resourceToScenarios(
            listOf(
                """Feature: My Test feature 0

  Scenario: String concatenation
    Given a static string
    And another static string
    When they are concatenated
    Then the result is
""",
                """Feature: My Test feature 1

  Scenario: String concatenation
    Given a static string
    And another static string
    When they are concatenated
    Then the result is
"""
            )
        )

        assertEquals(2, features.size)

        features.forEachIndexed { index, feature ->
            assertEquals("My Test feature $index", feature.name)

            assertEquals(1, feature.steps.size)
            val scenario = feature.steps.first()

            assertTrue(scenario is NestedScenarioStep)
            scenario as NestedScenarioStep

            val execution = scenario.execution()
            assertTrue(execution is NestedScenarioStepExecution)
            execution as NestedScenarioStepExecution

            val builtScenario = execution.scenario()
            assertEquals("String concatenation", builtScenario.name)

            assertEquals(4, builtScenario.steps.size)
            val step1 = builtScenario.steps[0]
            val step2 = builtScenario.steps[1]
            val step3 = builtScenario.steps[2]
            val step4 = builtScenario.steps[3]

            assertEquals("Given a static string", step1.name.value)
            assertEquals("And another static string", step2.name.value)
            assertEquals("When they are concatenated", step3.name.value)
            assertEquals("Then the result is", step4.name.value)

            assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step4.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
            assertEquals(1, (step4.future as AssertableStepResult<*, *, *>).assertions.size)

            val assertion = (step4.future as AssertableStepResult<*, *, *>).assertions.first()

            val exception = assertThrows<AssertionFailedError> {
                AssertionsBuilder("", StepName("...")).run {
                    @Suppress("UNCHECKED_CAST")
                    (assertion as AssertionsBuilder.(String) -> Unit)("hello")
                }
            }

            assertEquals("a static stringanother static string", exception.expected.value)
        }
    }

    @Test
    fun `two scenarios in a same feature generate one scenario with two nested scenarios`() {
        val features = builder.resourceToScenarios(
            listOf(
                """Feature: My Test feature

  Scenario: String concatenation 0
    Given a static string
    And another static string
    When they are concatenated
    Then the result is
""",
                """Feature: My Test feature

  Scenario: String concatenation 1
    Given a static string
    And another static string
    When they are concatenated
    Then the result is
"""
            )
        )

        assertEquals(1, features.size)
        val feature = features.first()
        assertEquals("My Test feature", feature.name)

        assertEquals(2, feature.steps.size)
        feature.steps.forEachIndexed { index, scenario ->

            assertTrue(scenario is NestedScenarioStep)
            scenario as NestedScenarioStep

            val execution = scenario.execution()
            assertTrue(execution is NestedScenarioStepExecution)
            execution as NestedScenarioStepExecution

            val builtScenario = execution.scenario()
            assertEquals("String concatenation $index", builtScenario.name)

            assertEquals(4, builtScenario.steps.size)
            val step1 = builtScenario.steps[0]
            val step2 = builtScenario.steps[1]
            val step3 = builtScenario.steps[2]
            val step4 = builtScenario.steps[3]

            assertEquals("Given a static string", step1.name.value)
            assertEquals("And another static string", step2.name.value)
            assertEquals("When they are concatenated", step3.name.value)
            assertEquals("Then the result is", step4.name.value)

            assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step4.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
            assertEquals(1, (step4.future as AssertableStepResult<*, *, *>).assertions.size)

            val assertion = (step4.future as AssertableStepResult<*, *, *>).assertions.first()

            val exception = assertThrows<AssertionFailedError> {
                AssertionsBuilder("", StepName("...")).run {
                    @Suppress("UNCHECKED_CAST")
                    (assertion as AssertionsBuilder.(String) -> Unit)("hello")
                }
            }

            assertEquals("a static stringanother static string", exception.expected.value)
        }
    }

    @Test
    fun `steps variables are extracted from gherkin text`() {
        val features = builder.resourceToScenarios(
            listOf(
                """Feature: Play with strings

  Scenario: Reverse a string
    Given string hello world
    When it is reversed
    Then it becomes dlrow olleh
""",
                """Feature: Play with numbers

  Scenario: Do some maths
    Given number 82
    When it is divided by 4
    And multiplied by sum of 1 and 2
    Then the result is 61.5

  Scenario: Do some maths with null values
    Given number 82
    When it is divided by 4
    And multiplied by sum of null and "null"
    Then the result is 0.0
""",
                """Feature: Play with booleans

  Scenario: And operation
    Given boolean true and boolean false
    When a logical AND is performed
    Then the boolean result is false

  Scenario: Or operation
    Given boolean true and boolean false
    When a logical OR is performed
    Then the boolean result is true
""",
            ),
        )

        assertEquals(3, features.size)

        features[0].let { feature ->

            assertEquals("Play with strings", feature.name)
            assertEquals(1, feature.steps.size)

            val scenario = feature.steps.first()

            assertTrue(scenario is NestedScenarioStep)
            scenario as NestedScenarioStep

            val execution = scenario.execution()
            assertTrue(execution is NestedScenarioStepExecution)
            execution as NestedScenarioStepExecution

            val builtScenario = execution.scenario()
            assertEquals("Reverse a string", builtScenario.name)

            assertEquals(3, builtScenario.steps.size)
            val step1 = builtScenario.steps[0]
            val step2 = builtScenario.steps[1]
            val step3 = builtScenario.steps[2]

            assertEquals("Given string hello world", step1.name.value)
            assertEquals("When it is reversed", step2.name.value)
            assertEquals("Then it becomes dlrow olleh", step3.name.value)

            assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
            assertEquals(1, (step3.future as AssertableStepResult<*, *, *>).assertions.size)

            val assertion = (step3.future as AssertableStepResult<*, *, *>).assertions.first()

            val exception = assertThrows<AssertionFailedError> {
                AssertionsBuilder("", StepName("...")).run {
                    @Suppress("UNCHECKED_CAST")
                    (assertion as AssertionsBuilder.(String) -> Unit)("blah")
                }
            }

            assertEquals("dlrow olleh", exception.expected.value)
        }

        features[1].let { feature ->

            assertEquals("Play with numbers", feature.name)
            assertEquals(2, feature.steps.size)

            feature.steps.first().let { scenario ->

                assertTrue(scenario is NestedScenarioStep)
                scenario as NestedScenarioStep

                val scenarioExecution = scenario.execution()
                assertTrue(scenarioExecution is NestedScenarioStepExecution)
                scenarioExecution as NestedScenarioStepExecution

                val builtScenarioExecutionScenario = scenarioExecution.scenario()
                assertEquals("Do some maths", builtScenarioExecutionScenario.name)

                assertEquals(4, builtScenarioExecutionScenario.steps.size)
                val step1 = builtScenarioExecutionScenario.steps[0]
                val step2 = builtScenarioExecutionScenario.steps[1]
                val step3 = builtScenarioExecutionScenario.steps[2]
                val step4 = builtScenarioExecutionScenario.steps[3]

                assertEquals("Given number 82", step1.name.value)
                assertEquals("When it is divided by 4", step2.name.value)
                assertEquals("And multiplied by sum of 1 and 2", step3.name.value)
                assertEquals("Then the result is 61.5", step4.name.value)

                assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
                assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
                assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
                assertTrue((step4.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
                assertEquals(1, (step4.future as AssertableStepResult<*, *, *>).assertions.size)

                val assertion =
                    (step4.future as AssertableStepResult<*, *, *>).assertions.first()

                val exception = assertThrows<AssertionFailedError> {
                    AssertionsBuilder("", StepName("...")).run {
                        @Suppress("UNCHECKED_CAST")
                        (assertion as AssertionsBuilder.(Double) -> Unit)(3.0)
                    }
                }

                assertEquals(61.5, exception.expected.value)
            }

            feature.steps.last().let { scenario ->

                assertTrue(scenario is NestedScenarioStep)
                scenario as NestedScenarioStep

                val scenarioExecution = scenario.execution()
                assertTrue(scenarioExecution is NestedScenarioStepExecution)
                scenarioExecution as NestedScenarioStepExecution

                val builtScenarioExecutionScenario = scenarioExecution.scenario()
                assertEquals("Do some maths with null values", builtScenarioExecutionScenario.name)

                assertEquals(4, builtScenarioExecutionScenario.steps.size)
                val step1 = builtScenarioExecutionScenario.steps[0]
                val step2 = builtScenarioExecutionScenario.steps[1]
                val step3 = builtScenarioExecutionScenario.steps[2]
                val step4 = builtScenarioExecutionScenario.steps[3]

                assertEquals("Given number 82", step1.name.value)
                assertEquals("When it is divided by 4", step2.name.value)
                assertEquals("And multiplied by sum of null and \"null\"", step3.name.value)
                assertEquals("Then the result is 0.0", step4.name.value)

                assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
                assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
                assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
                assertTrue((step4.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
                assertEquals(1, (step4.future as AssertableStepResult<*, *, *>).assertions.size)

                val assertion =
                    (step4.future as AssertableStepResult<*, *, *>).assertions.first()

                val exception = assertThrows<AssertionFailedError> {
                    AssertionsBuilder("", StepName("...")).run {
                        @Suppress("UNCHECKED_CAST")
                        (assertion as AssertionsBuilder.(Double) -> Unit)(3.0)
                    }
                }

                assertEquals(0.0, exception.expected.value)
            }
        }

        features[2].let { feature ->

            assertEquals("Play with booleans", feature.name)
            assertEquals(2, feature.steps.size)

            feature.steps.first().let { scenario ->


                assertTrue(scenario is NestedScenarioStep)
                scenario as NestedScenarioStep

                val scenarioExecution = scenario.execution()
                assertTrue(scenarioExecution is NestedScenarioStepExecution)
                scenarioExecution as NestedScenarioStepExecution

                val builtScenarioExecutionScenario = scenarioExecution.scenario()
                assertEquals("And operation", builtScenarioExecutionScenario.name)

                assertEquals(3, builtScenarioExecutionScenario.steps.size)
                val step1 = builtScenarioExecutionScenario.steps[0]
                val step2 = builtScenarioExecutionScenario.steps[1]
                val step3 = builtScenarioExecutionScenario.steps[2]

                assertEquals("Given boolean true and boolean false", step1.name.value)
                assertEquals("When a logical AND is performed", step2.name.value)
                assertEquals("Then the boolean result is false", step3.name.value)

                assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
                assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
                assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
                assertEquals(1, (step3.future as AssertableStepResult<*, *, *>).assertions.size)

                val assertion = (step3.future as AssertableStepResult<*, *, *>).assertions.first()

                val exception = assertThrows<AssertionFailedError> {
                    AssertionsBuilder("", StepName("...")).run {
                        @Suppress("UNCHECKED_CAST")
                        (assertion as AssertionsBuilder.(Boolean) -> Unit)(true)
                    }
                }

                assertEquals(false, exception.expected.value)
            }

            feature.steps.last().let { scenario ->

                assertTrue(scenario is NestedScenarioStep)
                scenario as NestedScenarioStep

                val scenarioExecution = scenario.execution()
                assertTrue(scenarioExecution is NestedScenarioStepExecution)
                scenarioExecution as NestedScenarioStepExecution

                val builtScenarioExecutionScenario = scenarioExecution.scenario()
                assertEquals("Or operation", builtScenarioExecutionScenario.name)

                assertEquals(3, builtScenarioExecutionScenario.steps.size)
                val step1 = builtScenarioExecutionScenario.steps[0]
                val step2 = builtScenarioExecutionScenario.steps[1]
                val step3 = builtScenarioExecutionScenario.steps[2]

                assertEquals("Given boolean true and boolean false", step1.name.value)
                assertEquals("When a logical OR is performed", step2.name.value)
                assertEquals("Then the boolean result is true", step3.name.value)

                assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
                assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
                assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
                assertEquals(1, (step3.future as AssertableStepResult<*, *, *>).assertions.size)

                val assertion = (step3.future as AssertableStepResult<*, *, *>).assertions.first()

                val exception = assertThrows<AssertionFailedError> {
                    AssertionsBuilder("", StepName("...")).run {
                        @Suppress("UNCHECKED_CAST")
                        (assertion as AssertionsBuilder.(Boolean) -> Unit)(false)
                    }
                }

                assertEquals(true, exception.expected.value)
            }
        }
    }

    @Test
    fun `additionnal argument is extracted from gherkin step`() {
        val features = builder.resourceToScenarios(
            listOf(
                "Feature: Play with strings\n\n" +
                        "  Scenario: Reverse a string\n" +
                        "    Given string hello world\n" +
                        "    When it is reversed\n" +
                        "    Then it becomes\n" +
                        "    \"\"\"\n" +
                        "    dlrow olleh\n" +
                        "    \"\"\"\n"
            ),
        )

        assertEquals(1, features.size)

        features[0].let { feature ->

            assertEquals("Play with strings", feature.name)
            assertEquals(1, feature.steps.size)

            val scenario = feature.steps.first()

            assertTrue(scenario is NestedScenarioStep)
            scenario as NestedScenarioStep

            val execution = scenario.execution()
            assertTrue(execution is NestedScenarioStepExecution)
            execution as NestedScenarioStepExecution

            val builtScenario = execution.scenario()
            assertEquals("Reverse a string", builtScenario.name)

            assertEquals(3, builtScenario.steps.size)
            val step1 = builtScenario.steps[0]
            val step2 = builtScenario.steps[1]
            val step3 = builtScenario.steps[2]

            assertEquals("Given string hello world", step1.name.value)
            assertEquals("When it is reversed", step2.name.value)
            assertEquals("Then it becomes", step3.name.value)

            assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
            assertEquals(1, (step3.future as AssertableStepResult<*, *, *>).assertions.size)

            val assertion = (step3.future as AssertableStepResult<*, *, *>).assertions.first()

            val exception = assertThrows<AssertionFailedError> {
                AssertionsBuilder("", StepName("...")).run {
                    @Suppress("UNCHECKED_CAST")
                    (assertion as AssertionsBuilder.(String) -> Unit)("blah")
                }
            }

            assertEquals("dlrow olleh", exception.expected.value)
        }
    }

    @Test
    fun `an execution builder can be passed from step to step to complete it before launching it`() {
        val features = builder.resourceToScenarios(
            listOf(
                """Feature: Mathematics

  Scenario: Sum numbers
    Given numbers 1 and 2
    And numbers 3 and 4
    When they are added
    Then the result is 10
""",
            ),
        )

        assertEquals(1, features.size)

        features.first().let { feature ->

            assertEquals("Mathematics", feature.name)
            assertEquals(1, feature.steps.size)

            val scenario = feature.steps.first()

            assertTrue(scenario is NestedScenarioStep)
            scenario as NestedScenarioStep

            val execution = scenario.execution()
            assertTrue(execution is NestedScenarioStepExecution)
            execution as NestedScenarioStepExecution

            val builtScenario = execution.scenario()
            assertEquals("Sum numbers", builtScenario.name)

            assertEquals(4, builtScenario.steps.size)
            val step1 = builtScenario.steps[0]
            val step2 = builtScenario.steps[1]
            val step3 = builtScenario.steps[2]
            val step4 = builtScenario.steps[3]

            assertEquals("Given numbers 1 and 2", step1.name.value)
            assertEquals("And numbers 3 and 4", step2.name.value)
            assertEquals("When they are added", step3.name.value)
            assertEquals("Then the result is 10", step4.name.value)

            assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step4.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
            assertEquals(1, (step4.future as AssertableStepResult<*, *, *>).assertions.size)

            val assertion = (step4.future as AssertableStepResult<*, *, *>).assertions.first()

            val exception = assertThrows<AssertionFailedError> {
                AssertionsBuilder("", StepName("...")).run {
                    @Suppress("UNCHECKED_CAST")
                    (assertion as AssertionsBuilder.(Long) -> Unit)(27)
                }
            }

            assertEquals(10.0, exception.expected.value)
        }

    }

    @Test
    fun `an execution builder can be passed as type receiver from step to step to complete it before launching it`() {
        val features = builder.resourceToScenarios(
            listOf(
                """Feature: Mathematics

  Scenario: Multiply numbers
    Given nums 1 and 2
    And nums 3 and 4
    When they are multiplied
    Then the result is 24
""",
            ),
        )

        assertEquals(1, features.size)

        features.first().let { feature ->

            assertEquals("Mathematics", feature.name)
            assertEquals(1, feature.steps.size)

            val scenario = feature.steps.first()

            assertTrue(scenario is NestedScenarioStep)
            scenario as NestedScenarioStep

            val execution = scenario.execution()
            assertTrue(execution is NestedScenarioStepExecution)
            execution as NestedScenarioStepExecution

            val builtScenario = execution.scenario()
            assertEquals("Multiply numbers", builtScenario.name)

            assertEquals(4, builtScenario.steps.size)
            val step1 = builtScenario.steps[0]
            val step2 = builtScenario.steps[1]
            val step3 = builtScenario.steps[2]
            val step4 = builtScenario.steps[3]

            assertEquals("Given nums 1 and 2", step1.name.value)
            assertEquals("And nums 3 and 4", step2.name.value)
            assertEquals("When they are multiplied", step3.name.value)
            assertEquals("Then the result is 24", step4.name.value)

            assertTrue((step1.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step2.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step3.future as AssertableStepResult<*, *, *>).assertions.isEmpty())
            assertTrue((step4.future as AssertableStepResult<*, *, *>).assertions.isNotEmpty())
            assertEquals(1, (step4.future as AssertableStepResult<*, *, *>).assertions.size)

            val assertion = (step4.future as AssertableStepResult<*, *, *>).assertions.first()

            val exception = assertThrows<AssertionFailedError> {
                AssertionsBuilder("", StepName("...")).run {
                    @Suppress("UNCHECKED_CAST")
                    (assertion as AssertionsBuilder.(Long) -> Unit)(27)
                }
            }

            assertEquals(24.0, exception.expected.value)
        }

    }

    @Test
    fun `a definition with an incompatbile receiver throws an exception`() {
        val features = builder.resourceToScenarios(
            listOf(
                """Feature: Wrong definitions

  Scenario: Wrong definition
    Given a definition with an incompatible receiver
""",
            ),
        )

        assertEquals(1, features.size)

        features.first().let { feature ->

            assertEquals("Wrong definitions", feature.name)
            assertEquals(1, feature.steps.size)

            val scenario = feature.steps.first()

            assertTrue(scenario is NestedScenarioStep)
            scenario as NestedScenarioStep

            val exception = assertThrows<IllegalArgumentException> { scenario.execution() }

            assertEquals("""Could not call step "a definition with an incompatible receiver" of scenario "Wrong definition" due to mis construction of your scenario, receiver parameter (kotlin.String) cannot be set for function wrongDefinition""", exception.message)
        }

    }

    @Test
    fun `a definition with an incompatbile parameter throws an exception`() {
        val features = builder.resourceToScenarios(
            listOf(
                """Feature: Wrong definitions

  Scenario: Wrong definition
    Given a definition
    When calling a definition with wrong parameter
""",
            ),
        )

        assertEquals(1, features.size)

        features.first().let { feature ->

            assertEquals("Wrong definitions", feature.name)
            assertEquals(1, feature.steps.size)

            val scenario = feature.steps.first()

            assertTrue(scenario is NestedScenarioStep)
            scenario as NestedScenarioStep

            val exception = assertThrows<IllegalArgumentException> { scenario.execution() }

            assertEquals("""Could not call step "calling a definition with wrong parameter" of scenario "Wrong definition" due to mis construction of your scenario, parameter param cannot be set for function wrongParameter because previous step does not return type of param (kotlin.String)""", exception.message)
        }

    }

    @Test
    fun `a definition with wrong number of parameter throws an exception`() {
        val features = builder.resourceToScenarios(
            listOf(
                """Feature: Wrong definitions 1

  Scenario: Wrong definition
    Given a definition with wrong number of parameter one and two
""",
            ),
        )

        assertEquals(1, features.size)

        features.first().let { feature ->

            assertEquals("Wrong definitions 1", feature.name)
            assertEquals(1, feature.steps.size)

            val scenario = feature.steps.first()

            assertTrue(scenario is NestedScenarioStep)
            scenario as NestedScenarioStep

            val exception = assertThrows<IllegalArgumentException> { scenario.execution() }

            assertEquals("""Could not build step "a definition with wrong number of parameter one and two" of scenario "Wrong definition": 
expected 3 value parameters, got 2 value parameters for function wrongNumberOfParameter""", exception.message)
        }

    }

    @Test
    fun `a definition with a not existing implementation throws an exception`() {
        val features = builder.resourceToScenarios(
            listOf(
                """Feature: Wrong definitions

  Scenario: Wrong definition
    Given a definition that does not have any implementation
""",
            ),
        )

        assertEquals(1, features.size)

        features.first().let { feature ->

            assertEquals("Wrong definitions", feature.name)
            assertEquals(1, feature.steps.size)

            val scenario = feature.steps.first()

            assertTrue(scenario is NestedScenarioStep)
            scenario as NestedScenarioStep

            val exception = assertThrows<IllegalArgumentException> { scenario.execution() }

            assertEquals("""Could not find any implementation for step "a definition that does not have any implementation" of scenario "Wrong definition"""", exception.message)
        }

    }

    @Test
    fun `an invalid gherkin format throws an exception`() {
        val exception = assertThrows<IllegalArgumentException> {

            builder.resourceToScenarios(
                listOf(
                    """blablabla
                    Feature: Wrong definitions
                    
                      Scenario: Wrong definition
                        Given nums 1 and 2
                    """.trimIndent(),
                ),
            )
        }

        assertTrue(exception.message?.startsWith("Gherkin parse error: ") ?: false)

    }

}