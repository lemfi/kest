@file:Suppress("unused")

package com.github.lemfi.kest.cadence.builder

import com.github.lemfi.kest.cadence.executor.ActivityExecution
import com.github.lemfi.kest.cadence.model.cadenceProperty
import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.model.Execution
import com.uber.cadence.context.ContextPropagator
import java.lang.reflect.Type
import kotlin.reflect.KFunction

class ActivityCallExecutionBuilder<RESULT>(private val type: Type) :
    ExecutionBuilder<RESULT> {

    lateinit var domain: String
    lateinit var tasklist: String

    private lateinit var activity: KFunction<RESULT>
    private var params: Array<out Any?>? = null
    private var contextPropagators: List<ContextPropagator>? = null

    @Suppress("MemberVisibilityCanBePrivate")
    var host = cadenceProperty { host }

    @Suppress("MemberVisibilityCanBePrivate")
    var port = cadenceProperty { port }

    fun activity(activity: KFunction<RESULT>, vararg parameters: Any?) {
        this.activity = activity
        this.params = parameters
    }

    fun contextPropagators(vararg contextPropagators: ContextPropagator?) {
        this.params = contextPropagators
    }

    override fun toExecution(): Execution<RESULT> {
        return ActivityExecution(
            host, port, domain, tasklist, activity, params, contextPropagators, type
        )
    }
}