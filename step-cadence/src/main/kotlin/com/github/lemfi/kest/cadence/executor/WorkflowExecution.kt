package com.github.lemfi.kest.cadence.executor

import com.github.lemfi.kest.core.model.Execution
import com.uber.cadence.client.WorkflowClient
import com.uber.cadence.client.WorkflowClientOptions
import com.uber.cadence.client.WorkflowOptions
import com.uber.cadence.context.ContextPropagator
import com.uber.cadence.serviceclient.ClientOptions
import com.uber.cadence.serviceclient.WorkflowServiceTChannel
import com.uber.cadence.worker.WorkerFactory
import com.uber.cadence.worker.WorkerOptions
import java.time.Duration
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaType

internal class WorkflowExecution<RESULT>(
    private val cadenceHost: String,
    private val cadencePort: Int,
    private val cadenceDomain: String,
    private val tasklist: String,

    private val workflow: KFunction<RESULT>,
    private val params: Array<out Any?>?,

    private val activities: List<Pair<Any, String>>?,
    private val contextPropagators: List<ContextPropagator>?,

    ) : Execution<RESULT>() {

    @Suppress("unchecked_cast")
    override fun execute(): RESULT {

        val workflowClass = Class.forName(workflow.parameters[0].type::javaType.get().typeName)

        activities
            ?.map { it.second }
            ?.toSet()
            ?.let { tasklists ->
                tasklists.forEach { tasklist ->
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
                                tasklist, WorkerOptions.defaultInstance()
                            )

                            worker.registerActivitiesImplementations(*activities.filter { it.second == tasklist }
                                .map { it.first }.toTypedArray())

                        }.start()
                }
            }


        val parameterTypes = workflow.parameters.subList(1, workflow.parameters.size).also { parameterTypes ->
            if ((params?.size
                    ?: 0) != parameterTypes.size
            ) throw IllegalArgumentException(
                "Wrong number of parameter for activity, expected [${
                    parameterTypes.map { it.type }.joinToString(", ")
                }] got ${params?.toList()}"
            )
        }.map { Class.forName(it.type.javaType.typeName) }.takeIf { it.isNotEmpty() }

        val method = parameterTypes?.let { workflowClass.getMethod(workflow.name, *parameterTypes.toTypedArray()) }
            ?: workflowClass.getMethod(workflow.name)

        return WorkflowClient.newInstance(
            WorkflowServiceTChannel(
                ClientOptions.newBuilder()
                    .setHost(cadenceHost)
                    .setPort(cadencePort)
                    .build()
            ),
            WorkflowClientOptions.newBuilder().setDomain(cadenceDomain).build()
        )
            .newWorkflowStub(workflowClass, WorkflowOptions.Builder()
                .setExecutionStartToCloseTimeout(Duration.ofMinutes(3))
                .apply {
                    contextPropagators?.let {
                        setContextPropagators(it)
                    }
                }
                .setTaskList(tasklist)
                .build()).let {

                if (params != null) {
                    method.invoke(it, *params)
                } else {
                    method.invoke(it)
                }
            } as RESULT
    }
}