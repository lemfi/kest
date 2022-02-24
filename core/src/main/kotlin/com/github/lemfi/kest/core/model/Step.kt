@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.core.model

import com.github.lemfi.kest.core.builder.AssertionsBuilder

sealed class Step<T> {
    abstract val scenarioName: String
    abstract val name: StepName?
    abstract var execution: () -> Execution<T>
    abstract val retry: RetryStep?

    abstract var postExecution: IStepPostExecution<T, T>
}

class StandaloneStep<T>(
    override val scenarioName: String,
    override val name: StepName?,
    override val retry: RetryStep?,
) : Step<T>() {

    override var postExecution: IStepPostExecution<T, T> = StandaloneStepPostExecution<T, T, T>(this, null) { t -> t }

    override lateinit var execution: () -> Execution<T>
}

class NestedScenarioStep<T>(
    override val scenarioName: String,
    override val name: StepName?,
    override val retry: RetryStep?,
) : Step<T>() {

    override var postExecution: IStepPostExecution<T, T> = NestedScenarioStepPostExecution(this, null) { t -> t }

    override lateinit var execution: () -> Execution<T>
}

@JvmInline
value class StepName(val value: String)

typealias StepPostExecution<T> = StandaloneStepPostExecution<T, T, T>

sealed class IStepPostExecution<T, R>(
    private val step: Step<*>,
    private val pe: IStepPostExecution<*, T>?,
    private val transformer: (T) -> R
) {

    private var resSet = false
    private var failed: Throwable? = null
    private var res: T? = null

    @Suppress("unchecked_cast")
    private val result: () -> R = {

        val tryResolveResult: (() -> R) -> R = {
            try {
                it()
            } catch (e: Throwable) {
                with(
                    StepResultFailure(
                        step = step,
                        cause = e,
                    )
                ) {
                    setFailed(this)
                    throw this
                }
            }
        }

        if (failed != null) {
            throw StepResultFailure(step = step, cause = failed!!)
        } else if (resSet) tryResolveResult {
            transformer(res as T)
        } else if (pe != null) tryResolveResult {
            transformer(pe.result())
        } else throw StepResultFailure(
            step,
            """
                |Step "${step.name?.value ?: step}" was not played yet! 
                |You may use its result only in another step body
                |""".trimMargin()
        )
    }


    operator fun invoke() = result()

    fun setResult(t: T) {
        resSet = true
        var parent: IStepPostExecution<*, *>? = pe
        while (parent != null) {
            parent.resSet = true
            parent = parent.pe
        }
        res = t
    }

    fun setFailed(e: Throwable) {
        failed = e
        resSet = true
        var parent: IStepPostExecution<*, *>? = pe
        while (parent != null) {
            parent.setFailed(e)
            parent = parent.pe
        }
    }

    fun isFailed() = resSet && failed != null
    fun isSuccess() = resSet && failed == null
}

class StandaloneStepPostExecution<I : Any?, T, R>(
    private val step: Step<*>,
    private val pe: StandaloneStepPostExecution<I, *, T>?,
    transformer: (T) -> R
) : IStepPostExecution<T, R>(step, pe, transformer) {

    infix fun <M> `map result to`(mapper: (R) -> M) = StandaloneStepPostExecution(step, this, mapper)

    val assertions: MutableList<AssertionsBuilder.(I) -> Unit> = mutableListOf()

    fun addAssertion(assertion: AssertionsBuilder.(I) -> Unit) {
        pe?.addAssertion(assertion) ?: assertions.add(assertion)
    }
}

class NestedScenarioStepPostExecution<T, R>(
    private val step: Step<*>,
    pe: NestedScenarioStepPostExecution<*, T>?,
    transformer: (T) -> R
) : IStepPostExecution<T, R>(step, pe, transformer) {

    infix fun <M> `map result to`(mapper: (R) -> M) = NestedScenarioStepPostExecution(step, this, mapper)

}

data class RetryStep(
    val retries: Int = 3,
    val delay: Long = 1000L,
)

val Int.times: RetryStep get() = RetryStep(retries = this)
val Int.ms: Long get() = this.toLong()
val Int.seconds: Long get() = this * 1000L
infix fun RetryStep.`by intervals of`(milliseconds: Long) = copy(delay = milliseconds)

class StepResultFailure(
    val step: Step<*>,
    override val message: String? = """Could not get result from previous step "${step.name?.value ?: step}"""",
    override val cause: Throwable? = null
) : Throwable(message, cause)