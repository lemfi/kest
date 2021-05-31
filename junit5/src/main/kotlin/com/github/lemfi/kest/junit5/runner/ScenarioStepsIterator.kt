package com.github.lemfi.kest.junit5.runner

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.NestedScenario
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.core.model.Step
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.opentest4j.AssertionFailedError

class ScenarioStepsIterator(private val scenario: IScenario, private val parent: ScenarioStepsIterator? = null): Iterator<DynamicNode>, Iterable<DynamicNode> {

    val steps = scenario.steps.iterator()

    private var success: Boolean = true
    private set(value) {
        field = value
        parent?.also { it.success = false }
    }

    override fun hasNext(): Boolean {
        if (scenario is NestedScenario<*> && !steps.hasNext() ) {
            scenario.resolve()
        }
        return success && steps.hasNext()
    }

    override fun next(): DynamicNode = steps.next().toDynamicNode()

    override fun iterator(): Iterator<DynamicNode> {
        return this
    }

    @Suppress("unchecked_cast")
    private fun NestedScenario<*>.resolve() =
        (this as NestedScenario<Any>).parentStep.postExecution.setResult(result())

    fun Step<*>.toDynamicNode() =
        if (this is NestedScenarioStep<*>) {
            DynamicContainer.dynamicContainer(
                name?.value ?: "anonymous step",
                ScenarioStepsIterator((execution() as NestedScenarioStepExecution).scenario(), this@ScenarioStepsIterator)
            )
        }
        else DynamicTest.dynamicTest(name?.value ?: "anonymous step") {
            try {
                run()
            } catch (e: AssertionFailedError) {
                success = false
                throw e
            }
        }
}
