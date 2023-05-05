package com.github.lemfi.kest.gherkin.core

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.builder.NestedScenarioExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.builder.StandaloneScenarioBuilder
import com.github.lemfi.kest.core.cli.nestedScenario
import com.github.lemfi.kest.core.cli.step
import com.github.lemfi.kest.core.model.DefaultStepName
import com.github.lemfi.kest.core.model.IStepResult
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.model.Step
import com.github.lemfi.kest.core.model.StepName
import io.cucumber.gherkin.GherkinParser
import io.cucumber.messages.types.Envelope
import io.cucumber.messages.types.Source
import io.cucumber.messages.types.SourceMediaType
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.util.UUID
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.kotlinFunction

internal data class GherkinExpression(
    val label: String,
    val sentence: String,
)

class GherkinScenarioBuilder(
    stepDefinitionsPackages: Collection<String> = gherkinProperty { stepDefinitions },
) {

    private val reflections = Reflections(
        ConfigurationBuilder()
            .forPackages(*stepDefinitionsPackages.toTypedArray())
            .setScanners(
                Scanners.MethodsAnnotated
            )
    )

    private val functions: List<Pair<KFunction<*>, GherkinExpression>> =
        findFunctionsWithMatchingAnnotation<Given>() +
                findFunctionsWithMatchingAnnotation<When>() +
                findFunctionsWithMatchingAnnotation<Then>() +
                findFunctionsWithMatchingAnnotation<And>() +
                findFunctionsWithMatchingAnnotation<But>()

    private inline fun <reified T : Annotation> findFunctionsWithMatchingAnnotation() = reflections
        .getMethodsAnnotatedWith(T::class.java)
        .map { method ->
            method.kotlinFunction!! to method.kotlinFunction!!.findAnnotation<T>()!!.let { annotation ->
                GherkinExpression(
                    label = annotation.annotationClass.findAnnotation<Gherkin>()!!.label,
                    sentence = annotation
                        .annotationClass.memberProperties
                        .first { it.name == "sentence" }
                        .call(annotation).toString()
                )
            }
        }

    fun resourceToScenarios(sources: List<String>): List<Scenario> {

        val features = discoverFeatures(sources)

        return features.map { (feature, scenarios) ->

            StandaloneScenarioBuilder(feature)
                .apply {
                    scenarios.flatMap { (_, pickles) ->
                        pickles.map { buildScenario(it) }
                    }
                }.toScenario()
        }
    }

    private fun StandaloneScenarioBuilder.buildScenario(scenarioDescription: GherkinScenarioDescription) =
        nestedScenario(scenarioDescription.title) {
            var previousGherkinStep: GherkinStep? = null

            scenarioDescription.steps.forEach { step ->
                previousGherkinStep = buildGherkinStep(step, previousGherkinStep)
                if (previousGherkinStep!!.built!!::class.isSubclassOf(ExecutionBuilder::class)) {
                    step {}
                    steps.last().useGherkinStepNameAsKestStepName(previousGherkinStep!!.gherkinAssertion)
                }
            }
        }

    private fun NestedScenarioExecutionBuilder<Unit>.buildGherkinStep(
        step: GherkinStepDescription,
        previousGherkinStep: GherkinStep?
    ): GherkinStep =

        findImplementation(step.sentence).let { (function, expression) ->
            fillParameters(function, expression, step, previousGherkinStep).let { parameters ->

                GherkinStep(
                    gherkinAssertion = "${step.keyword ?: expression.label}${step.sentence}",
                    built = function.call(
                        *parameters.validateAndTransformParameters(
                            step.sentence,
                            scenarioName,
                            function
                        )
                    )
                ).apply {

                    if (built is IStepResult<*, *>) {
                        steps.last().useGherkinStepNameAsKestStepName(gherkinAssertion)
                    }
                }
            }

        }

    private fun NestedScenarioExecutionBuilder<Unit>.fillParameters(
        function: KFunction<*>,
        expression: GherkinExpression,
        step: GherkinStepDescription,
        previousGherkinStep: GherkinStep?,
    ): List<Any?> {

        val parameters = expression.toParameters(step)

        if (function.parameters.count { it.kind == KParameter.Kind.VALUE } < parameters.size) {
            throw IllegalArgumentException(
                """Could not build step "${step.sentence}" of scenario "$scenarioName": 
            |expected ${function.parameters.size} value parameters, got ${parameters.size} value parameters for function ${function.name}""".trimMargin()
            )
        }

        function.parameters.reversed().drop(parameters.size).forEach { kParameter ->

            val parameterKlass = kParameter.type.classifier as KClass<*>

            when (kParameter.kind) {

                KParameter.Kind.INSTANCE ->
                    parameters.add(0, parameterKlass.createEnclosingInstance())

                KParameter.Kind.EXTENSION_RECEIVER ->

                    when {

                        parameterKlass == ScenarioBuilder::class -> parameters.add(0, this)

                        previousGherkinStep.isSubclassOf(parameterKlass) ->
                            parameters.add(0, previousGherkinStep!!.built)

                        kParameter.type.isMarkedNullable -> parameters.add(0, null)

                        else -> throw IllegalArgumentException("""Could not call step "${step.sentence}" of scenario "$scenarioName" due to mis construction of your scenario, receiver parameter (${kParameter.type}) cannot be set for function ${function.name}""")
                    }

                KParameter.Kind.VALUE ->
                    when {

                        previousGherkinStep.isSubclassOf(parameterKlass) ->
                            parameters.add(0, previousGherkinStep!!.built)

                        kParameter.type.isMarkedNullable -> parameters.add(0, null)

                        else -> throw IllegalArgumentException("""Could not call step "${step.sentence}" of scenario "$scenarioName" due to mis construction of your scenario, parameter ${kParameter.name} cannot be set for function ${function.name} because previous step does not return type of ${kParameter.name} (${kParameter.type})""")
                    }
            }
        }
        return parameters
    }

    private fun NestedScenarioExecutionBuilder<Unit>.findImplementation(text: String) =
        functions
            .firstOrNull { (_, e) -> e.validates(text) }
            ?: throw IllegalArgumentException("""Could not find any implementation for step "$text" of scenario "$scenarioName"""")

    private fun KClass<*>.createEnclosingInstance() = runCatching { createInstance() }.getOrElse { objectInstance }

    private fun discoverFeatures(resources: List<String>) = resources
        .map { resource ->
            val envelope = Envelope.of(
                Source(
                    UUID.randomUUID().toString(), // not used
                    resource,
                    SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN,
                )
            )

            val parsedGherkin = GherkinParser
                .builder()
                .includeGherkinDocument(true)
                .includePickles(true)
                .includeSource(true)
                .build()
                .parse(envelope)
                .collect(Collectors.toList())

            val errors = parsedGherkin.firstOrNull { it.parseError.isPresent }

            if (errors != null) {
                throw IllegalArgumentException("Gherkin parse error: ${errors.parseError.get().message}")
            }

            val pickles = parsedGherkin.filter { it.pickle.isPresent }
            val gherkinDoc = parsedGherkin.first { it.gherkinDocument.isPresent }

            gherkinDoc.gherkinDocument.get() to pickles.map { pickle ->
                pickle
                    .pickle
                    .get()
                    .let { scenario ->
                        GherkinScenarioDescription(
                            title = scenario.name,
                            steps = scenario
                                .steps
                                .map { step ->
                                    GherkinStepDescription(
                                        sentence = step.text,
                                        additionalArgument = runCatching { step.argument.get().docString.get().content }.getOrNull(),
                                        keyword = runCatching {
                                            gherkinDoc
                                                .gherkinDocument.get()
                                                .feature.get()
                                                .children.first { it.scenario.get().name == scenario.name }
                                                .scenario.get()
                                                .steps.first { it.text == step.text }.keyword
                                        }.getOrNull()
                                    )
                                }
                        )
                    }
            }
        }
        .groupBy { (gherkinDoc, _) -> gherkinDoc.feature.getOrNull()?.name ?: "" }
        .filterNot { it.key.isBlank() }

    private fun Step<*>.useGherkinStepNameAsKestStepName(gherkinName: String) {
        if (name is DefaultStepName) {
            this::class
                .memberProperties
                .first { it.name == "name" }
                .javaField
                ?.also { nameField ->
                    nameField.isAccessible = true
                    nameField.set(this, StepName(gherkinName))
                }
        }
    }

    private fun GherkinExpression.validates(input: String) =
        Pattern.compile(sentence).matcher(input).matches()

    private fun GherkinExpression.toParameters(step: GherkinStepDescription): MutableList<Any?> =
        sentence
            .toRegex()
            .toPattern()
            .matcher(step.sentence)
            .let { m ->
                m.find()
                (1..m.groupCount()).map {
                    m.group(it)
                }
            }
            .toMutableList()
            .apply {
                if (step.additionalArgument != null) {
                    add(step.additionalArgument)
                }
            }.toMutableList()

    private fun List<Any?>.validateAndTransformParameters(
        stepName: String,
        scenarioName: String,
        function: KFunction<*>
    ): Array<Any?> {

        if (size != function.parameters.size) throw IllegalArgumentException(
            """Could not build step "$stepName" of scenario "$scenarioName": 
            |expected ${function.parameters.size} parameters, got $size parameters for function ${function.name}""".trimMargin()
        )

        return mapIndexed { index, paramValue ->

            val param = function.parameters[index]

            when {
                paramValue == null ->
                    if (!param.type.isMarkedNullable) throw NullPointerException(
                        "expected non null value for parameter ${param.name} of function ${function.name}"
                    ) else null

                paramValue::class == param.type.classifier -> paramValue
                param.kind == KParameter.Kind.INSTANCE -> paramValue
                param.kind == KParameter.Kind.EXTENSION_RECEIVER -> paramValue
                else -> when {
                    (param.type.classifier as KClass<*>).isSubclassOf(IStepResult::class) -> paramValue
                    (param.type.classifier as KClass<*>).supertypes.any { it.classifier == IStepResult::class } -> paramValue
                    param.type.classifier as KClass<*> == Long::class -> transform(
                        function,
                        param,
                        paramValue.toString()
                    ) { toLong() }

                    param.type.classifier as KClass<*> == Int::class -> transform(
                        function,
                        param,
                        paramValue.toString()
                    ) { toInt() }

                    param.type.classifier as KClass<*> == Float::class -> transform(
                        function,
                        param,
                        paramValue.toString()
                    ) { toFloat() }

                    param.type.classifier as KClass<*> == Double::class -> transform(
                        function,
                        param,
                        paramValue.toString()
                    ) {
                        toDouble()
                    }

                    param.type.classifier as KClass<*> == Boolean::class -> transform(
                        function,
                        param,
                        paramValue.toString()
                    ) {
                        toBooleanStrict()
                    }

                    else -> throw IllegalArgumentException("only String, Long, Int, Float, Double, Boolean types are supported as gherkin parameters, got ${function.parameters[index].type.classifier}")
                }
            }


        }.toTypedArray()
    }

    private inline fun <reified T> transform(
        function: KFunction<*>,
        parameter: KParameter,
        value: String,
        transformer: String.() -> T?
    ): T? =
        try {
            if (parameter.type.isMarkedNullable && (value == "null" || value == """"null"""")) null
            else transformer(value)
        } catch (e: Throwable) {
            throw IllegalArgumentException("Could not transform $value to ${parameter.type.classifier} for parameter ${parameter.name} of function ${function.name}")
        }
}

private fun GherkinStep?.isSubclassOf(kls: KClass<*>) =
    this?.built != null && built::class.isSubclassOf(kls)

private data class GherkinStep(val gherkinAssertion: String, val built: Any?)
private data class GherkinStepDescription(val sentence: String, val keyword: String?, val additionalArgument: String?)
private data class GherkinScenarioDescription(val title: String, val steps: List<GherkinStepDescription>)
