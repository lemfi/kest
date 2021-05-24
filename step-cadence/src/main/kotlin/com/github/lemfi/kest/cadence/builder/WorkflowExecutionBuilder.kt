package com.github.lemfi.kest.cadence.builder

import com.github.lemfi.kest.cadence.executor.WorkflowExecution
import com.github.lemfi.kest.cadence.model.cadenceProperty
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.StepName
import com.uber.cadence.context.ContextPropagator
import kotlin.reflect.KFunction

class WorkflowExecutionBuilder<RESULT> : ExecutionBuilder<RESULT>() {

    private var name: StepName? = null
    fun name(l: ()->String) {
        name = StepName(l())
    }

    lateinit var domain: String
    lateinit var tasklist: String

    private lateinit var workflow: KFunction<RESULT>
    private var params: Array<out Any?>? = null
    private var contextPropagators: List<ContextPropagator>? = null

    private var activities: List<Pair<Any, String>>? = null

    var host = cadenceProperty { host }
    var port = cadenceProperty { port }


    fun workflow(workflow: KFunction<RESULT>, vararg parameters: Any?) {
        this.workflow = workflow
        this.params = parameters
    }

    fun contextPropagators(vararg contextPropagator: ContextPropagator) {
        this.contextPropagators = contextPropagator.toList()
    }

    fun activities(vararg activity: Pair<Any, String>) {
        activities = activity.toList()
    }

    override fun build(): Execution<RESULT> {
        return WorkflowExecution(
            name, host, port, domain, tasklist, workflow, params, activities, contextPropagators
        )
    }
}