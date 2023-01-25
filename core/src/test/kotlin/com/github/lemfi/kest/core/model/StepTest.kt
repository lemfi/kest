package com.github.lemfi.kest.core.model

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.cli.`is false`
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StepTest {

    @Test
    fun `resolve a step that was not played yet`() {
        val step = mockk<StandaloneStep<String>>()

        every { step.name } returns object : IStepName {
            override val value = "a step"
        }

        val stepRes = StepPostExecution<String>(
            step = step,
            pe = null,
            transformer = { t -> t }
        )

        val exception = assertThrows<StepResultFailure> {
            stepRes()
        }

        Assertions.assertEquals(
            """|
            |Step "a step" was not played yet! 
            |You may use its result only in another step body
            |""".trimMargin(), exception.message
        )
    }

    @Test
    fun `resolve a step that failed`() {
        val step = mockk<StandaloneStep<String>>()

        every { step.name } returns object : IStepName {
            override val value = "a step"
        }

        val stepRes = StepPostExecution<String>(
            step = step,
            pe = null,
            transformer = { t -> t }
        )

        stepRes.setFailed(IllegalArgumentException("something failed"))

        val exception = assertThrows<StepResultFailure> {
            stepRes()
        }

        Assertions.assertEquals(
            """|
            |Could not get result from previous step "a step"
            |Assertions failed for step
            |""".trimMargin(), exception.message
        )
    }

    @Test
    fun `resolve a successful step`() {
        val step = mockk<StandaloneStep<String>>()

        every { step.name } returns StepName("a step")

        val stepRes = StepPostExecution<String>(
            step = step,
            pe = null,
            transformer = { t -> t }
        )

        stepRes.setResult("successful step")

        val res = stepRes()

        Assertions.assertEquals("successful step", res)
    }

    @Test
    fun `resolve and transform a successful step`() {
        val step = mockk<StandaloneStep<String>>()

        every { step.name } returns StepName("a step")

        val stepRes = StepPostExecution<String>(
            step = step,
            pe = null,
            transformer = { t -> "$t is transformed" }
        )

        stepRes.setResult("successful step")

        val res = stepRes()

        Assertions.assertEquals("successful step is transformed", res)
    }

    @Test
    fun `resolve and transform with failure a successful step`() {
        val step = mockk<StandaloneStep<String>>()

        every { step.name } returns object : IStepName {
            override val value = "a step"
        }

        val stepRes = StepPostExecution<String>(
            step = step,
            pe = null,
            transformer = { _ -> throw IllegalArgumentException("make transformation fail") }
        )

        stepRes.setResult("successful step")

        val exception = assertThrows<StepResultFailure> {
            stepRes()
        }

        Assertions.assertEquals(
            """|
            |Could not get result from previous step "a step"
            |Could not compute result
            |""".trimMargin(), exception.message
        )
    }

    @Test
    fun `resolve and transform a successful step with multiple levels of transformation`() {
        val step1 = mockk<StandaloneStep<String>>()

        every { step1.name } returns StepName("step 1")

        val stepRes2 = StepPostExecution<String>(
            step = step1,
            pe = null,
            transformer = { t -> t }
        )

        val stepRes1 = StepPostExecution(
            step = step1,
            pe = stepRes2,
            transformer = { t -> "$t is transformed" }
        )

        stepRes2.setResult("successful step")

        val res = stepRes1()

        Assertions.assertEquals("successful step is transformed", res)
    }

    @Test
    fun `all parents are resolved when a post execution is sucessfully resolved`() {
        val step1 = mockk<StandaloneStep<String>>()

        every { step1.name } returns StepName("step 1")

        val stepRes3 = StepPostExecution<String>(
            step = step1,
            pe = null,
            transformer = { t -> t }
        )

        val stepRes2 = StepPostExecution(
            step = step1,
            pe = stepRes3,
            transformer = { t -> t }
        )

        val stepRes1 = StepPostExecution(
            step = step1,
            pe = stepRes2,
            transformer = { t -> "$t is transformed" }
        )

        stepRes1.setResult("successful step")

        val res = stepRes1()

        Assertions.assertEquals("successful step is transformed", res)
        Assertions.assertTrue(stepRes2.isSuccess())
        Assertions.assertTrue(stepRes3.isSuccess())
    }

    @Test
    fun `all parents are marked as failed when a post execution is marked as failed`() {
        val step1 = mockk<StandaloneStep<String>>()

        every { step1.name } returns object : IStepName {
            override val value = "step 1"
        }

        val stepRes3 = StepPostExecution<String>(
            step = step1,
            pe = null,
            transformer = { t -> t }
        )

        val stepRes2 = StepPostExecution(
            step = step1,
            pe = stepRes3,
            transformer = { t -> t }
        )

        val stepRes1 = StepPostExecution(
            step = step1,
            pe = stepRes2,
            transformer = { t -> "$t is transformed" }
        )

        stepRes1.setFailed(java.lang.IllegalArgumentException("fail..."))

        val exception = assertThrows<StepResultFailure> {
            stepRes1()
        }

        Assertions.assertEquals(
            """|
            |Could not get result from previous step "step 1"
            |Assertions failed for step
            |""".trimMargin(),
            exception.message
        )

        Assertions.assertTrue(stepRes2.isFailed())
        Assertions.assertTrue(stepRes3.isFailed())
    }

    @Test
    fun `add assertion to standlalone step`() {
        val step = mockk<StandaloneStep<String>>()

        every { step.name } returns StepName("a step")
        val stepRes = StepPostExecution<String>(
            step = step,
            pe = null,
            transformer = { t -> t }
        )

        val assertion: AssertionsBuilder.(String) -> Unit =
            { true.`is false` }

        stepRes.addAssertion(assertion)

        Assertions.assertEquals(listOf(assertion), stepRes.assertions)
    }

    @Test
    fun `add assertion to standlalone step with a parent`() {
        val step = mockk<StandaloneStep<String>>()

        every { step.name } returns StepName("a step")
        val stepRes2 = StepPostExecution<String>(
            step = step,
            pe = null,
            transformer = { t -> t }
        )
        val stepRes1 = StepPostExecution(
            step = step,
            pe = stepRes2,
            transformer = { t -> t }
        )

        val assertion: AssertionsBuilder.(String) -> Unit =
            { true.`is false` }

        stepRes1.addAssertion(assertion)

        Assertions.assertEquals(listOf(assertion), stepRes2.assertions)
        Assertions.assertEquals(emptyList<AssertionsBuilder.(String) -> Unit>(), stepRes1.assertions)
    }


    @Test
    fun `map standlalone step result`() {
        val step = mockk<StandaloneStep<String>>()

        every { step.name } returns StepName("a step")
        val stepRes = StepPostExecution<String>(
            step = step,
            pe = null,
            transformer = { t -> t }
        )
        stepRes.setResult("successful step")

        val res = stepRes `map result to` { "$it is transformed" }

        Assertions.assertEquals("successful step is transformed", res())
    }

    @Test
    fun `map nested scenario step result`() {
        val step = mockk<StandaloneStep<String>>()

        every { step.name } returns StepName("a step")
        val stepRes = NestedScenarioStepPostExecution<String, String>(
            step = step,
            pe = null,
            transformer = { t -> t }
        )
        stepRes.setResult("successful step")

        val res = stepRes `map result to` { "$it is transformed" }

        Assertions.assertEquals("successful step is transformed", res())
    }

    @Test
    fun `build a retry step`() {
        Assertions.assertEquals(RetryStep(10, 100), 10.times `by intervals of` 100.ms)
        Assertions.assertEquals(RetryStep(20, 1000), 20.times `by intervals of` 1.seconds)
    }
}