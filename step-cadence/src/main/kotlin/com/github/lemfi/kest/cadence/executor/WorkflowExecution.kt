package com.github.lemfi.kest.cadence.executor

import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.ExecutionDescription
import com.github.lemfi.kest.core.model.StepName
import com.uber.cadence.client.WorkflowClient
import com.uber.cadence.client.WorkflowOptions
import com.uber.cadence.common.RetryOptions
import com.uber.cadence.context.ContextPropagator
import com.uber.cadence.worker.Worker
import com.uber.cadence.worker.WorkerOptions
import org.opentest4j.AssertionFailedError
import java.time.Duration
import kotlin.reflect.KFunction
import kotlin.reflect.javaType

class WorkflowExecution<RESULT>(
    override val description: ExecutionDescription?,
    private val cadenceHost: String,
    private val cadencePort: Int,
    private val cadenceDomain: String,
    private val tasklist: String,

    private val workflow: KFunction<RESULT>,
    private val params: Array<out Any?>?,

    private val activities: List<Pair<Any, String>>?,
    private val contextPropagators: List<ContextPropagator>?,

    ) : Execution<RESULT>() {

    @ExperimentalStdlibApi
    @Suppress("unchecked_cast")
    override fun execute(): RESULT {

        val workflowClass = Class.forName(workflow.parameters[0].type::javaType.get().typeName)

        activities
            ?.map { it.second }
            ?.toSet()
            ?.let {
                it.forEach { tasklist ->
                    Worker.Factory(cadenceHost, cadencePort, cadenceDomain).apply {
                        val worker = newWorker(
                            tasklist, WorkerOptions
                                .Builder()
                                .setReportActivityFailureRetryOptions(
                                    RetryOptions.Builder()
                                        .setInitialInterval(Duration.ofSeconds(5))
                                        .setExpiration(Duration.ofSeconds(30))
                                        .setMaximumInterval(Duration.ofSeconds(10))
                                        .setMaximumAttempts(2)
                                        .build()
                                )
                                .build()
                        )

                        worker.registerActivitiesImplementations(*activities.filter { it.second == tasklist }
                            .map { it.first }.toTypedArray())

                    }.start()
                }
            }


        val parameterTypes = workflow.parameters.subList(1, workflow.parameters.size).also {
            if ((params?.size
                    ?: 0) != it.size
            ) throw AssertionFailedError(
                "Wrong number of parameter for activity, expected [${
                    it.map { it.type }.joinToString(", ")
                }] got ${params?.toList()}"
            )
        }.map { Class.forName(it.type::javaType.get().typeName) }.takeIf { it.isNotEmpty() }

        val method = parameterTypes?.let { workflowClass.getMethod(workflow.name, *parameterTypes.toTypedArray()) }
            ?: workflowClass.getMethod(workflow.name)

        return WorkflowClient.newInstance(cadenceHost, cadencePort, cadenceDomain)
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