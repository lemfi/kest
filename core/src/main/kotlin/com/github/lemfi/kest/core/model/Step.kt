package com.github.lemfi.kest.core.model

import com.github.lemfi.kest.core.builder.AssertionsBuilder

sealed class Step<T> {
    abstract val scenarioName: ScenarioName
    abstract val name: StepName?
    abstract var execution: () -> Execution<T>
    abstract val retry: RetryStep?

    var postExecution: StepPostExecution<T> = StepPostExecution(null) { t -> t }
}

class StandaloneStep<T>(
    override val scenarioName: ScenarioName,
    override val name: StepName?,
    override val retry: RetryStep?,
): Step<T>() {

    override lateinit var execution: () -> Execution<T>
}

class NestedScenarioStep<T>(
    override val scenarioName: ScenarioName,
    override val name: StepName?,
    override val retry: RetryStep?,
): Step<T>() {

    override lateinit var execution: () -> Execution<T>
}

@JvmInline
value class StepName(val value: String)

typealias StepPostExecution<T> = IStepPostExecution<T, T, T>

class IStepPostExecution<I: Any?, T, R>(
    private val pe: IStepPostExecution<I, *, T>?,
    private val transformer: (T) -> R
) {

    constructor(asyncResult: () -> R) : this(null, { asyncResult() }) {
        resSet = true
    }

    private var resSet = false
    private var res: T? = null

    @Suppress("unchecked_cast")
    private val result: () -> R = {
        if (resSet) transformer(res as T)
        else if (pe != null) transformer(pe.result())
        else throw IllegalAccessException("Step not played yet!")
    }

    infix fun <M> `map result to`(l: (R) -> M): IStepPostExecution<I, R, M> = IStepPostExecution(this, l)

    operator fun invoke() = result()

    fun setResult(t: T) {
        resSet = true
        res = t
    }

    val assertions: MutableList<AssertionsBuilder.(I) -> Unit> = mutableListOf()

    fun addAssertion(assertion: AssertionsBuilder.(I) -> Unit) {
        pe?.addAssertion(assertion) ?: assertions.add(assertion)
    }
}

data class RetryStep(
    val retries: Int = 3,
    val delay: Long = 1000L,
)

val Int.times: RetryStep get() = RetryStep(retries = this)
val Int.ms: Long get() = this.toLong()
val Int.seconds: Long get() = this * 1000L
infix fun RetryStep.`delayed by`(milliseconds: Long) = copy(delay = milliseconds)