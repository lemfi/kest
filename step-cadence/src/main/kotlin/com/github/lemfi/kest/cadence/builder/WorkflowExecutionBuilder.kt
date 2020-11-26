package com.github.lemfi.kest.cadence.builder

import com.github.lemfi.kest.cadence.executor.WorkflowExecution
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.uber.cadence.context.ContextPropagator
import kotlin.properties.Delegates
import kotlin.reflect.KFunction

class WorkflowExecutionBuilder<RESULT>: ExecutionBuilder<RESULT>() {

    lateinit var cadenceHost: String
    var cadencePort by Delegates.notNull<Int>()
    lateinit var cadenceDomain: String
    lateinit var tasklist: String

    lateinit var workflow: KFunction<RESULT>
    private var params: Array<out Any?>? = null
    private var contextPropagators: List<ContextPropagator>? = null

    private var activities: List<Pair<Any, String>>? = null

    private var withResult: RESULT.()->Unit = {}

    fun parameters(vararg parameters: Any?) {
        this.params = parameters
    }
    fun contextPropagators(vararg contextPropagator: ContextPropagator) {
        this.contextPropagators = contextPropagator.toList()
    }
    fun activities(vararg activity: Pair<Any, String>) {
        activities = activity.toList()
    }

    fun withResult(l: RESULT.()->Unit) { withResult = l }

    override fun build(): Execution<RESULT> {
        return WorkflowExecution(
                cadenceHost, cadencePort, cadenceDomain, tasklist, workflow, params, activities, contextPropagators, withResult
        )
    }
}