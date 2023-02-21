package com.github.lemfi.kest.gherkin.core

import com.github.lemfi.kest.core.model.StandaloneStepPostExecution

interface GherkinContext

private val gherkinContextThreadLocal = ThreadLocal.withInitial { ContextHolder() }

@Suppress("UNCHECKED_CAST")
infix fun <R, T: GherkinContext> StandaloneStepPostExecution<*, *, R>.`set gherkin scenario context`(l: (T?, R) -> T) = apply {

    gherkinContextThreadLocal.get().context.add {
        l(it as T?, this())
    }
}

@Suppress("UNCHECKED_CAST")
fun <T: GherkinContext?> `get gherkin scenario context`() = gherkinContextThreadLocal
    .get()
    .context
    .fold(null as T) { acc, context ->
        context(acc) as T
    }

private class ContextHolder {
    var context: MutableList<(GherkinContext?)->GherkinContext> = mutableListOf()
}

fun cleanGherkinContext() = gherkinContextThreadLocal.set(ContextHolder())