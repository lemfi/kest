package com.github.lemfi.kest.cadence.builder

import com.github.lemfi.kest.cadence.executor.ActivityExecution
import com.github.lemfi.kest.cadence.model.cadenceProperty
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.StepName
import com.uber.cadence.context.ContextPropagator
import kotlin.reflect.KFunction

class ActivityCallExecutionBuilder<RESULT>(private val cls: Class<RESULT>) : ExecutionBuilder<RESULT>() {

    private var name: StepName? = null
    fun name(l: ()->String) {
        name = StepName(l())
    }

    lateinit var domain: String
    lateinit var tasklist: String

    lateinit private var activity: KFunction<RESULT>
    private var params: Array<out Any?>? = null
    private var contextPropagators: List<ContextPropagator>? = null


    var host = cadenceProperty { host }
    var port = cadenceProperty { port }

    fun activity(activity: KFunction<RESULT>, vararg parameters: Any?) {
        this.activity = activity
        this.params = parameters
    }

    fun contextPropagators(vararg contextPropagators: ContextPropagator?) {
        this.params = contextPropagators
    }

    override fun build(): Execution<RESULT> {
        return ActivityExecution(
            name, host, port, domain, tasklist, cls, activity, params, contextPropagators
        )
    }
}