package com.github.lemfi.kest.core.model

import com.github.lemfi.kest.core.builder.AssertionsBuilder

sealed class Step<T> {
    abstract val scenarioName: ScenarioName
    abstract val name: StepName?
    abstract var execution: () -> Execution<T>
    abstract val retry: RetryStep?

    abstract var postExecution: IStepPostExecution<T, T>
}

class StandaloneStep<T>(
    override val scenarioName: ScenarioName,
    override val name: StepName?,
    override val retry: RetryStep?,
): Step<T>() {

    override var postExecution: IStepPostExecution<T, T> = StandaloneStepPostExecution<T, T, T>(null) { t -> t }

    override lateinit var execution: () -> Execution<T>
}

class NestedScenarioStep<T>(
    override val scenarioName: ScenarioName,
    override val name: StepName?,
    override val retry: RetryStep?,
): Step<T>() {

    override var postExecution: IStepPostExecution<T, T> = NestedScenarioStepPostExecution(null) { t -> t }

    override lateinit var execution: () -> Execution<T>
}

@JvmInline
value class StepName(val value: String)

typealias StepPostExecution<T> = StandaloneStepPostExecution<T, T, T>

sealed class IStepPostExecution<T, R>(
    private val pe: IStepPostExecution<*, T>?,
    private val transformer: (T) -> R
) {

    private var resSet = false
    private var res: T? = null

    @Suppress("unchecked_cast")
    private val result: () -> R = {
        if (resSet) transformer(res as T)
        else if (pe != null) transformer(pe.result())
        else throw IllegalAccessException("Step not played yet!")
    }

    operator fun invoke() = result()

    fun setResult(t: T) {
        resSet = true
        res = t
    }
}
class StandaloneStepPostExecution<I: Any?, T, R>(
    private val pe: StandaloneStepPostExecution<I, *, T>?,
    transformer: (T) -> R
): IStepPostExecution<T, R>(pe, transformer) {

    infix fun <M> `map result to`(mapper: (R) -> M) = StandaloneStepPostExecution(this, mapper)

    val assertions: MutableList<AssertionsBuilder.(I) -> Unit> = mutableListOf()

    fun addAssertion(assertion: AssertionsBuilder.(I) -> Unit) {
        pe?.addAssertion(assertion) ?: assertions.add(assertion)
    }
}

class NestedScenarioStepPostExecution<T, R>(
    pe: NestedScenarioStepPostExecution<*, T>?,
    transformer: (T) -> R
): IStepPostExecution<T, R>(pe, transformer) {

    infix fun <M> `map result to`(mapper: (R) -> M) = NestedScenarioStepPostExecution(this, mapper)

}

data class RetryStep(
    val retries: Int = 3,
    val delay: Long = 1000L,
)

val Int.times: RetryStep get() = RetryStep(retries = this)
val Int.ms: Long get() = this.toLong()
val Int.seconds: Long get() = this * 1000L
infix fun RetryStep.`delayed by`(milliseconds: Long) = copy(delay = milliseconds)