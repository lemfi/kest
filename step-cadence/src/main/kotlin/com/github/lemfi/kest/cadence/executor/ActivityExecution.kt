package com.github.lemfi.kest.cadence.executor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.model.Execution
import com.google.gson.Gson
import com.uber.cadence.activity.ActivityOptions
import com.uber.cadence.client.WorkflowClient
import com.uber.cadence.client.WorkflowClientOptions
import com.uber.cadence.client.WorkflowOptions
import com.uber.cadence.common.RetryOptions
import com.uber.cadence.context.ContextPropagator
import com.uber.cadence.serviceclient.ClientOptions
import com.uber.cadence.serviceclient.WorkflowServiceTChannel
import com.uber.cadence.worker.WorkerFactory
import com.uber.cadence.worker.WorkerOptions
import com.uber.cadence.workflow.Workflow
import com.uber.cadence.workflow.WorkflowMethod
import java.lang.reflect.Type
import java.time.Duration
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaType

internal class ActivityExecution<RESULT>(
    private val cadenceHost: String,
    private val cadencePort: Int,
    private val cadenceDomain: String,
    private val tasklist: String,

    private val activity: KFunction<RESULT>,
    private val params: Array<out Any?>?,
    private val contextPropagators: List<ContextPropagator>?,

    private val type: Type,

    ) : Execution<RESULT>() {

    @Suppress("unchecked_cast")
    override fun execute(): RESULT {

        WorkerFactory.newInstance(
            WorkflowClient.newInstance(
                WorkflowServiceTChannel(
                    ClientOptions.newBuilder()
                        .setHost(cadenceHost)
                        .setPort(cadencePort)
                        .build()
                ),
                WorkflowClientOptions.newBuilder().setDomain(cadenceDomain).build()
            )
        )
            .apply {
                val worker = newWorker(
                    "KEST_TL", WorkerOptions.defaultInstance()
                )

                worker.registerWorkflowImplementationTypes(com.github.lemfi.kest.cadence.executor.Workflow::class.java)

            }.start()

        val parameterTypes = activity.parameters.subList(1, activity.parameters.size).also { parameterTypes ->
            if ((params?.size ?: 0) != parameterTypes.size) throw IllegalArgumentException(
                "Wrong number of parameter for activity, expected [${
                    parameterTypes.map { it.type }.joinToString(", ")
                }] got ${params?.toList()}"
            )
        }

        return WorkflowClient.newInstance(
            WorkflowServiceTChannel(
                ClientOptions.newBuilder()
                    .setHost(cadenceHost)
                    .setPort(cadencePort)
                    .build()
            ),
            WorkflowClientOptions.newBuilder().setDomain(cadenceDomain).build()
        )
            .newWorkflowStub(IWorkflow::class.java, WorkflowOptions.Builder()
                .setExecutionStartToCloseTimeout(Duration.ofSeconds(30))
                .setTaskList("KEST_TL")
                .apply {
                    contextPropagators?.let {
                        setContextPropagators(it)
                    }
                }
                .build())

            .run(
                WorkflowParameter(
                    activity.parameters[0].type::javaType.get().typeName,
                    tasklist,
                    activity.name,
                    params?.mapIndexed { index, it ->
                        Parameter(
                            jacksonObjectMapper().writeValueAsString(it),
                            parameterTypes[index].type.javaType.typeName
                        )
                    })
            ).let {
                when (it) {
                    null -> null as RESULT
                    else -> Gson().fromJson(Gson().toJsonTree(it), type)
                }
            }


    }
}

class WorkflowParameter(
    val className: String,
    val tasklist: String,
    val function: String,
    val parameters: List<Parameter>?
)

interface IWorkflow {

    @WorkflowMethod
    fun run(parameters: WorkflowParameter): Any?
}

class Workflow : IWorkflow {

    override fun run(parameters: WorkflowParameter): Any? {

        val cls = Class.forName(parameters.className)

        val params = parameters.parameters?.map { jacksonObjectMapper().readValue(it.value, Class.forName(it.cls)) }
        val types = parameters.parameters?.map { Class.forName(it.cls) }

        val method = types?.let { cls.getMethod(parameters.function, *types.toTypedArray()) } ?: cls.getMethod(
            parameters.function
        )

        return if (params != null) {
            method.invoke(getActivity(cls, parameters.tasklist), *params.toTypedArray())
        } else {
            method.invoke(getActivity(cls, parameters.tasklist))
        }
    }

    private fun <T> getActivity(cls: Class<T>, tasklist: String): T {

        return Workflow.newActivityStub(
            cls,
            ActivityOptions.Builder()
                .setTaskList(tasklist)
                .setScheduleToCloseTimeout(Duration.ofSeconds(600))
                .setRetryOptions(
                    RetryOptions.Builder()
                        .setInitialInterval(Duration.ofSeconds(1))
                        .setExpiration(Duration.ofMinutes(1))
                        .setMaximumAttempts(5)
                        .build()
                )
                .build()
        )
    }
}

data class Parameter(
    val value: String?,
    val cls: String
)