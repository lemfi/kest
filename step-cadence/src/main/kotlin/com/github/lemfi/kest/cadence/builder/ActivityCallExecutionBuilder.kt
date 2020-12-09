package com.github.lemfi.kest.cadence.builder

import com.github.lemfi.kest.cadence.executor.ActivityExecution
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.uber.cadence.context.ContextPropagator
import kotlin.properties.Delegates
import kotlin.reflect.KFunction

class ActivityCallExecutionBuilder<RESULT>: ExecutionBuilder<RESULT>() {

    lateinit var cadenceHost: String
    var cadencePort by Delegates.notNull<Int>()
    lateinit var cadenceDomain: String
    lateinit var tasklist: String

    lateinit var activity: KFunction<RESULT>
    private var params: Array<out Any?>? = null
    private var contextPropagators: List<ContextPropagator>? = null

    private var withResult: RESULT.()->Unit = {}

    fun parameters(vararg parameters: Any?) {
        this.params = parameters
    }

    fun contextPropagators(vararg contextPropagators: ContextPropagator?) {
        this.params = contextPropagators
    }

    fun withResult(l: RESULT.()->Unit) { withResult = l }

    override fun build(): Execution<RESULT> {
        return ActivityExecution(
                cadenceHost, cadencePort, cadenceDomain, tasklist, activity, params, contextPropagators, withResult
        )
    }
}