package com.github.lemfi.kest.gherkin.junit5

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.NestedScenario
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepResultFailure
import com.github.lemfi.kest.gherkin.core.GherkinScenarioBuilder
import com.github.lemfi.kest.gherkin.junit5.discovery.FeaturesDiscoveryConfiguration
import com.github.lemfi.kest.gherkin.junit5.discovery.KestGherkinDiscoverySelector
import com.github.lemfi.kest.gherkin.junit5.discovery.toFeaturesDiscoveryConfiguration
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.ClasspathRootSelector
import org.junit.platform.engine.discovery.DirectorySelector
import org.junit.platform.engine.discovery.FileSelector
import org.junit.platform.engine.discovery.PackageSelector
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext
import org.junit.platform.engine.support.hierarchical.Node
import org.junit.platform.engine.support.hierarchical.Node.DynamicTestExecutor

private val logger = LoggerFactory.getLogger("JUNIT-RUNNER-Kest")

const val KestGherkinTestEngineName = "KestGherkinTestEngine"

class KestGherkinTestEngine : TestEngine {

    override fun getId() = KestGherkinTestEngineName

    override fun discover(engineDiscoveryRequest: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {

        val scenarios = mutableListOf<FeaturesDiscoveryConfiguration>()
            .apply {

                addAll(
                    engineDiscoveryRequest
                        .getSelectorsByType(ClassSelector::class.java)
                        .toFeaturesDiscoveryConfiguration()
                )
                addAll(
                    engineDiscoveryRequest
                        .getSelectorsByType(ClasspathRootSelector::class.java)
                        .toFeaturesDiscoveryConfiguration()
                )
                addAll(
                    engineDiscoveryRequest
                        .getSelectorsByType(PackageSelector::class.java)
                        .toFeaturesDiscoveryConfiguration()
                )
                addAll(
                    engineDiscoveryRequest
                        .getSelectorsByType(FileSelector::class.java)
                        .toFeaturesDiscoveryConfiguration()
                )
                addAll(
                    engineDiscoveryRequest
                        .getSelectorsByType(DirectorySelector::class.java)
                        .toFeaturesDiscoveryConfiguration()
                )
                addAll(engineDiscoveryRequest
                    .getSelectorsByType(KestGherkinDiscoverySelector::class.java)
                    .flatMap {
                        it.toFeaturesDiscoveryConfiguration(
                            it.getStepDefinitionsPackages(),
                            object : TestSource {},
                        )
                    }
                )

            }.flatMap { sourcesDefinition ->

                GherkinScenarioBuilder(sourcesDefinition.stepsPackages)
                    .resourceToScenarios(sourcesDefinition.features)
                    .map { scenario ->
                        FeatureTestDescriptor(scenario, sourcesDefinition.source)
                            .apply {
                                children.filterNot { child ->
                                    if (sourcesDefinition.filter.isNotEmpty()) sourcesDefinition.filter.any {
                                        child.uniqueId.toString().startsWith(it)
                                    } else true
                                }.let { testDescriptors ->
                                    testDescriptors.forEach { removeChild(it) }

                                }
                            }
                    }
                    .filter { feature ->
                        if (sourcesDefinition.filter.isNotEmpty()) sourcesDefinition.filter.any {
                            it.startsWith(feature.uniqueId.toString())
                        } else true
                    }

            }

        return KestGherkinTestEngineDescriptor(UniqueId.forEngine(id), id).apply {
            scenarios.forEach { scenario -> addChild(scenario) }
        }
    }


    override fun execute(executionRequest: ExecutionRequest) {
        executionRequest.engineExecutionListener?.dynamicTestRegistered(executionRequest.rootTestDescriptor)
        executionRequest.engineExecutionListener?.executionStarted(executionRequest.rootTestDescriptor)
        executionRequest.rootTestDescriptor.children.forEach { scenarioStepDescriptor ->
            if (scenarioStepDescriptor is FeatureTestDescriptor) {
                scenarioStepDescriptor.execute(
                    KestGherkinEngineExecutionContext(executionRequest.engineExecutionListener),
                    null
                )
            }
        }
        executionRequest.engineExecutionListener?.executionFinished(
            executionRequest.rootTestDescriptor,
            TestExecutionResult.successful()
        )

    }
}

class KestGherkinEngineExecutionContext(val listener: EngineExecutionListener?) : EngineExecutionContext

class KestGherkinTestEngineDescriptor(uniqueId: UniqueId, name: String) :
    EngineDescriptor(uniqueId, name),
    Node<KestGherkinEngineExecutionContext>

class FeatureTestDescriptor(private val scenario: IScenario, private val classSource: TestSource) :
    AbstractTestDescriptor(
        UniqueId.forEngine(KestGherkinTestEngineName).append("feature", scenario.name),
        scenario.name,
        classSource
    ), Node<KestGherkinEngineExecutionContext> {

    init {
        scenario.steps.forEach {

            if (it is NestedScenarioStep<*>) {
                addChild(NestedStepTestDescriptor(uniqueId, it, classSource, "scenario"))
            }
        }
    }

    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.CONTAINER

    override fun prune() {
        if (scenario.steps.isEmpty()) removeFromHierarchy()
    }

    override fun execute(
        context: KestGherkinEngineExecutionContext,
        dynamicTestExecutor: DynamicTestExecutor?
    ): KestGherkinEngineExecutionContext {

        context.listener?.dynamicTestRegistered(this)
        context.listener?.executionStarted(this)
        prepare(context)

        children.forEach {
            if (it is NestedStepTestDescriptor) it.execute(context, dynamicTestExecutor)
        }

        context.listener?.executionFinished(this, TestExecutionResult.successful())

        return context
    }

}

class NestedStepTestDescriptor(
    parentId: UniqueId,
    private val step: NestedScenarioStep<*>,
    private val classSource: TestSource,
    segmentType: String = "nestedScenario"
) :
    AbstractTestDescriptor(
        parentId.append(segmentType, step.name.value),
        step.name.value,
        classSource,
    ), Node<KestGherkinEngineExecutionContext> {

    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.CONTAINER

    private var scenario: NestedScenario<*>? = null

    override fun prune() { /* nested steps at scenario level will be pruned with specific kest mechanism */ }

    override fun prepare(context: KestGherkinEngineExecutionContext): KestGherkinEngineExecutionContext {
        runCatching {
            scenario = (step.execution() as NestedScenarioStepExecution).scenario()
            scenario!!.steps
        }.onSuccess { steps ->
            steps.forEach { step ->
                if (step is NestedScenarioStep<*>) {
                    NestedStepTestDescriptor(uniqueId, step, classSource).also {
                        addChild(it)
                    }
                } else {
                    StepTestDescriptor(uniqueId, step, classSource).also {
                        addChild(it)
                    }
                }
            }
        }.onFailure {
            logger.error(it.message, it)
            step.postExecution.setFailed(it)
        }.getOrThrow()

        return context
    }

    override fun execute(
        context: KestGherkinEngineExecutionContext,
        dynamicTestExecutor: DynamicTestExecutor?
    ): KestGherkinEngineExecutionContext =

        runCatching {
            context.listener?.dynamicTestRegistered(this)
            context.listener?.executionStarted(this)
            prepare(context)
        }.onFailure {
            context.listener?.executionFinished(this, TestExecutionResult.failed(it))
        }.getOrNull()?.let {

            children.forEach { testDescriptor ->
                if (testDescriptor is NestedStepTestDescriptor) testDescriptor.execute(it, dynamicTestExecutor)
                else if (testDescriptor is StepTestDescriptor) testDescriptor.execute(it, dynamicTestExecutor)
            }

            runCatching { scenario!!.resolve() }


            it.listener?.executionFinished(this, TestExecutionResult.successful())

            it
        } ?: context
}


class StepTestDescriptor(parentId: UniqueId, private val step: Step<*>, classSource: TestSource) :
    AbstractTestDescriptor(
        parentId.append("step", step.name.value),
        step.name.value,
        classSource,
    ), Node<KestGherkinEngineExecutionContext> {

    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.TEST

    override fun execute(
        context: KestGherkinEngineExecutionContext,
        dynamicTestExecutor: DynamicTestExecutor?
    ): KestGherkinEngineExecutionContext {
        context.listener?.dynamicTestRegistered(this)
        context.listener?.executionStarted(this)

        runCatching {
            step.run()
        }
            .onSuccess {
                context.listener?.executionFinished(this, TestExecutionResult.successful())
            }
            .onFailure {
                if (it is StepResultFailure) {
                    logger.warn(it.stackTraceToString())
                    context.listener?.executionSkipped(this, it.message)
                } else context.listener?.executionFinished(this, TestExecutionResult.failed(it))
            }

        return context
    }
}