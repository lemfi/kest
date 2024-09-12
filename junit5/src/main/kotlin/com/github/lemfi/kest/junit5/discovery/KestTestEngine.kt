package com.github.lemfi.kest.junit5.discovery

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.executor.NestedScenarioStepExecution
import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.IScenario
import com.github.lemfi.kest.core.model.NestedScenario
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepResultFailure
import org.junit.platform.engine.DiscoverySelector
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
import org.junit.platform.engine.discovery.MethodSelector
import org.junit.platform.engine.discovery.PackageSelector
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext
import org.junit.platform.engine.support.hierarchical.Node
import org.junit.platform.engine.support.hierarchical.Node.DynamicTestExecutor
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder

private val logger = LoggerFactory.getLogger("JUNIT-RUNNER-Kest")

const val KestTestEngineName = "KestTestEngine"

class KestTestEngine : TestEngine {

    override fun getId() = KestTestEngineName

    override fun discover(engineDiscoveryRequest: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {

        val scenarios = mutableListOf<ScenarioTestDescriptor>()
            .apply {

                addAll(
                    engineDiscoveryRequest
                        .getSelectorsByType(ClassSelector::class.java)
                        .toScenarios()
                )

                addAll(
                    engineDiscoveryRequest
                        .getSelectorsByType(MethodSelector::class.java)
                        .toScenarios()
                )

                //                addAll(
                //                    engineDiscoveryRequest
                //                        .getSelectorsByType(ClasspathRootSelector::class.java)
                //                        .toScenarios()
                //                )
                //                addAll(
                //                    engineDiscoveryRequest
                //                        .getSelectorsByType(PackageSelector::class.java)
                //                        .toScenarios()
                //                )
                //                addAll(
                //                    engineDiscoveryRequest
                //                        .getSelectorsByType(FileSelector::class.java)
                //                        .toScenarios()
                //                )
                //                addAll(
                //                    engineDiscoveryRequest
                //                        .getSelectorsByType(DirectorySelector::class.java)
                //                        .toScenarios()
                //                )

            }

        return KestTestEngineDescriptor(UniqueId.forEngine(id), "Kest").apply {
            scenarios.forEach { scenario ->
                scenario.setParent(this)
                addChild(scenario)
            }
        }
    }


    override fun execute(executionRequest: ExecutionRequest) {
        executionRequest.engineExecutionListener?.dynamicTestRegistered(executionRequest.rootTestDescriptor)
        executionRequest.engineExecutionListener?.executionStarted(executionRequest.rootTestDescriptor)
        executionRequest.rootTestDescriptor.children.forEach { scenarioStepDescriptor ->
            if (scenarioStepDescriptor is ScenarioTestDescriptor) {
                scenarioStepDescriptor.execute(
                    KestEngineExecutionContext(executionRequest.engineExecutionListener),
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

class KestEngineExecutionContext(val listener: EngineExecutionListener?) : EngineExecutionContext

class KestTestEngineDescriptor(uniqueId: UniqueId, name: String) :
    EngineDescriptor(uniqueId, name),
    Node<KestEngineExecutionContext>

class ScenarioTestDescriptor(private val scenario: IScenario, private val source: TestSource) :
    AbstractTestDescriptor(
        UniqueId.forEngine(KestTestEngineName).append("scenario", scenario.name),
        scenario.name,
        source
    ), Node<KestEngineExecutionContext> {

    init {
        scenario.steps.forEach { step ->

            if (step is NestedScenarioStep<*>) {
                addChild(NestedStepTestDescriptor(uniqueId, step, source, "scenario"))
            } else {
                addChild(StepTestDescriptor(uniqueId, step, source))
            }
        }
    }

    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.CONTAINER

    override fun prune() {
        if (scenario.steps.isEmpty()) removeFromHierarchy()
    }

    override fun execute(
        context: KestEngineExecutionContext,
        dynamicTestExecutor: DynamicTestExecutor?,
    ): KestEngineExecutionContext {

        context.listener?.dynamicTestRegistered(this)
        context.listener?.executionStarted(this)
        prepare(context)

        children.forEach {
            if (it is NestedStepTestDescriptor) {
                it.execute(context, dynamicTestExecutor)
            }
        }

        children.forEach { testDescriptor ->
            if (testDescriptor is NestedStepTestDescriptor) testDescriptor.execute(context, dynamicTestExecutor)
            else if (testDescriptor is StepTestDescriptor) testDescriptor.execute(context, dynamicTestExecutor)
        }
        context.listener?.executionFinished(this, TestExecutionResult.successful())

        return context
    }

}

class NestedStepTestDescriptor(
    parentId: UniqueId,
    private val step: NestedScenarioStep<*>,
    private val source: TestSource,
    segmentType: String = "nestedScenario",
) :
    AbstractTestDescriptor(
        parentId.append(segmentType, step.name.value),
        step.name.value,
        source,
    ), Node<KestEngineExecutionContext> {

    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.CONTAINER

    private var scenario: NestedScenario<*>? = null

    override fun prune() { /* nested steps at scenario level will be pruned with specific kest mechanism */
    }

    override fun prepare(context: KestEngineExecutionContext): KestEngineExecutionContext {
        runCatching {
            scenario = (step.execution() as NestedScenarioStepExecution).scenario()
            scenario!!.steps
        }.onSuccess { steps ->
            steps.forEach { step ->
                if (step is NestedScenarioStep<*>) {
                    NestedStepTestDescriptor(uniqueId, step, source).also {
                        addChild(it)
                    }
                } else {
                    StepTestDescriptor(uniqueId, step, source).also {
                        addChild(it)
                    }
                }
            }
        }.onFailure {
            logger.error(it.message, it)
            step.future.setFailed(it)
        }.getOrThrow()

        return context
    }

    override fun execute(
        context: KestEngineExecutionContext,
        dynamicTestExecutor: DynamicTestExecutor?,
    ): KestEngineExecutionContext =

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
    ), Node<KestEngineExecutionContext> {

    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.TEST

    override fun execute(
        context: KestEngineExecutionContext,
        dynamicTestExecutor: DynamicTestExecutor?,
    ): KestEngineExecutionContext {
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

internal fun DiscoverySelector.toClasses() =
    when (this) {

        is ClassSelector ->
            listOfNotNull(
                if (javaClass.declaredAnnotations.any { it is Kest }) javaClass
                else null
            )

        is ClasspathRootSelector -> Reflections(
            ConfigurationBuilder()
                .addUrls(classpathRoot.toURL())
                .setScanners(Scanners.TypesAnnotated)
        ).getTypesAnnotatedWith(Kest::class.java)

        is PackageSelector -> Reflections(
            ConfigurationBuilder()
                .forPackage(packageName)
                .filterInputsBy(FilterBuilder().includePackage(packageName))
                .setScanners(Scanners.TypesAnnotated)
        ).getTypesAnnotatedWith(Kest::class.java)

        else -> emptyList()
    }


@JvmName("classSelectorToSourceDefinition")
internal fun Collection<ClassSelector>.toScenarios(): List<ScenarioTestDescriptor> =

    flatMap { classSelector ->
        classSelector.javaClass.declaredMethods.filter {
            it.declaredAnnotations.any { it is Kest }
        }.mapNotNull {
            runCatching {
                ScenarioTestDescriptor(
                    it.invoke(classSelector.javaClass.getDeclaredConstructor().newInstance()) as Scenario,
                    ClassSource.from(classSelector.javaClass)
                )
            }
                .onFailure { it.printStackTrace() }
                .getOrNull()
        }
    }

@JvmName("methodSelectorToSourceDefinition")
internal fun Collection<MethodSelector>.toScenarios(): List<ScenarioTestDescriptor> =

    mapNotNull { methodSelector ->
        runCatching {
            ScenarioTestDescriptor(
                methodSelector.javaMethod.invoke(
                    methodSelector.javaClass.getDeclaredConstructor().newInstance()
                ) as Scenario,
                MethodSource.from(methodSelector.javaMethod)
            )
        }
            .getOrNull()
    }

//@JvmName("classpathRootSelectorToSourceDefinition")
//internal fun Collection<ClasspathRootSelector>.toScenarios() =
//    toClasses().flatMap { it.toFeaturesDiscoveryConfiguration() }
//
//@JvmName("packageSelectorToSourceDefinition")
//internal fun Collection<PackageSelector>.toScenarios() =
//    toClasses().flatMap { it.toFeaturesDiscoveryConfiguration() }
//
//@JvmName("fileSelectorToSourceDefinition")
//internal fun Collection<FileSelector>.toScenarios() =
//    map {
//        FeaturesDiscoveryConfiguration(
//            features = listOf(it.file.readText(Charset.defaultCharset())),
//            stepsPackages = gherkinProperty { stepDefinitions },
//            source = FileSource.from(it.file),
//        )
//    }
//
//@JvmName("directorySelectorToSourceDefinition")
//internal fun Collection<DirectorySelector>.toFeaturesDiscoveryConfiguration() =
//    map { directorySelector ->
//        FeaturesDiscoveryConfiguration(
//            features = directorySelector.directory
//                .walkTopDown()
//                .toList()
//                .mapNotNull { if (it.isDirectory) null else it.readText(Charset.defaultCharset()) },
//            stepsPackages = gherkinProperty { stepDefinitions },
//            source = DirectorySource.from(directorySelector.directory),
//        )
//    }