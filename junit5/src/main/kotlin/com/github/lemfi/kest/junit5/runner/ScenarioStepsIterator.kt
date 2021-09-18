package com.github.lemfi.kest.junit5.runner

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.model.*
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.opentest4j.TestAbortedException
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("JUNIT-RUNNER-Kest")

class ScenarioStepsIterator(private val scenario: IScenario): Iterator<DynamicNode>, Iterable<DynamicNode> {

    val steps = scenario.steps.iterator()

    override fun hasNext(): Boolean {
        if (scenario is NestedScenario<*> && !steps.hasNext() ) {
            scenario.resolve()
        }
        return steps.hasNext()
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
                ScenarioStepsIterator((execution() as NestedScenarioStepExecution).scenario())
            )
        }
        else DynamicTest.dynamicTest(name?.value ?: "anonymous step") {
            try {
                run()
            } catch (e: StepResultFailure) {
                logger.warn(e.message)
                throw TestAbortedException(e.message)
            }
        }
}
