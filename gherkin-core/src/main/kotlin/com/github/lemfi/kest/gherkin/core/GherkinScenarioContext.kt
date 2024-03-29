package com.github.lemfi.kest.gherkin.core

import com.github.lemfi.kest.core.model.IStepResult

interface GherkinContext

private val gherkinContextThreadLocal = ThreadLocal.withInitial { ContextHolder() }

@Suppress("UNCHECKED_CAST")
@Deprecated("use setGherkinScenarioContext instead", replaceWith = ReplaceWith("this setGherkinScenarioContext l"))
infix fun <STEP_POST_EXECUTION: IStepResult<*, RESULT>, RESULT, GHERKIN_CONTEXT : GherkinContext> STEP_POST_EXECUTION.`set gherkin scenario context`(l: (GHERKIN_CONTEXT?, RESULT) -> GHERKIN_CONTEXT): STEP_POST_EXECUTION =
    apply {

        gherkinContextThreadLocal.get().context.add { gherkinContext ->
            runCatching { this() }.getOrNull()?.let { l(gherkinContext as GHERKIN_CONTEXT?, it) } ?: gherkinContext
        }
    }

@Suppress("UNCHECKED_CAST")
infix fun <STEP_POST_EXECUTION: IStepResult<*, RESULT>, RESULT, GHERKIN_CONTEXT : GherkinContext> STEP_POST_EXECUTION.setGherkinScenarioContext(l: (GHERKIN_CONTEXT?, RESULT) -> GHERKIN_CONTEXT): STEP_POST_EXECUTION =
    apply {

        gherkinContextThreadLocal.get().context.add { gherkinContext ->
            runCatching { this() }.getOrNull()?.let { l(gherkinContext as GHERKIN_CONTEXT?, it) } ?: gherkinContext
        }
    }

@Suppress("UNCHECKED_CAST")
@Deprecated("use getGherkinScenarioContext()", replaceWith = ReplaceWith("getGherkinScenarioContext()"))
fun <GHERKIN_CONTEXT : GherkinContext?> `get gherkin scenario context`() = gherkinContextThreadLocal
    .get()
    .context
    .fold(null as GHERKIN_CONTEXT) { acc, context ->
        context(acc) as GHERKIN_CONTEXT
    }

@Suppress("UNCHECKED_CAST")
fun <GHERKIN_CONTEXT : GherkinContext?> getGherkinScenarioContext() = gherkinContextThreadLocal
    .get()
    .context
    .fold(null as GHERKIN_CONTEXT) { acc, context ->
        context(acc) as GHERKIN_CONTEXT
    }

private class ContextHolder {
    var context: MutableList<(GherkinContext?) -> GherkinContext?> = mutableListOf()
}

fun cleanGherkinContext() = gherkinContextThreadLocal.set(ContextHolder())