package com.github.lemfi.kest.junit5.runner

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.NestedScenario
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepResultFailure
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.opentest4j.TestAbortedException

private val logger = LoggerFactory.getLogger("JUNIT-RUNNER-Kest")

internal class ScenarioStepsIterator(private val scenario: IScenario) : Iterator<DynamicNode>, Iterable<DynamicNode> {

    private val steps = scenario.steps.iterator()

    override fun hasNext(): Boolean {
        return steps.hasNext()
    }

    override fun next(): DynamicNode = steps.next().toDynamicNode()

    override fun iterator(): Iterator<DynamicNode> {
        return this
    }

    private fun resolveScenario() {
        try {
            if (!steps.hasNext() && scenario is NestedScenario<*>) scenario.resolve()
        } catch (e: StepResultFailure) {
            // fail silently, will fail later...
        }
    }

    private fun Step<*>.toDynamicNode(): DynamicNode =
        if (this is NestedScenarioStep<*>) {
            runCatching {
                execution() as NestedScenarioStepExecution
            }
                .let {
                    if (it.isFailure) {
                        createDynamicTest(name?.value ?: "anonymous step") { throw it.exceptionOrNull()!! }
                    } else {
                        DynamicContainer.dynamicContainer(
                            name?.value ?: "anonymous step",
                            ScenarioStepsIterator(it.getOrNull()!!.scenario())
                        )
                    }
                }

        } else createDynamicTest(name?.value ?: "anonymous step") { run() }


    private fun createDynamicTest(name: String, test: () -> Unit) =
        DynamicTest.dynamicTest(name) {
            try {
                test()
            } catch (e: StepResultFailure) {
                logger.warn(e.message)
                throw TestAbortedException(e.message)
            } finally {
                resolveScenario()
            }
        }
}
