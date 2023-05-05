@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.core.model

import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.cli.run

sealed class Step<RESULT> {
    abstract val scenarioName: String
    abstract val name: IStepName
    abstract var execution: () -> Execution<RESULT>
    abstract val retry: RetryStep?

    @Deprecated("use stepResult instead", replaceWith = ReplaceWith("future"))
    abstract var postExecution: IStepResult<RESULT, RESULT>
    abstract var future: IStepResult<RESULT, RESULT>
}

class StandaloneStep<RESULT>(
    override val scenarioName: String,
    override val name: IStepName,
    override val retry: RetryStep?,
) : Step<RESULT>() {

    @Deprecated("use stepResult instead", replaceWith = ReplaceWith("future"))
    override var postExecution: IStepResult<RESULT, RESULT> = StandaloneStepResult(this, null) { t -> t }
    override var future: IStepResult<RESULT, RESULT> = StandaloneStepResult(this, null) { t -> t }

    override lateinit var execution: () -> Execution<RESULT>
}

class NestedScenarioStep<RESULT>(
    override val scenarioName: String,
    override val name: IStepName,
) : Step<RESULT>() {

    override val retry: RetryStep? = null

    @Deprecated("use stepResult instead", replaceWith = ReplaceWith("future"))
    override var postExecution: IStepResult<RESULT, RESULT> = NestedScenarioStepResult(this, null) { t -> t }
    override var future: IStepResult<RESULT, RESULT> = NestedScenarioStepResult(this, null) { t -> t }

    override lateinit var execution: () -> Execution<RESULT>
}

interface IStepName {
    val value: String
}

@JvmInline
value class StepName(override val value: String) : IStepName

@JvmInline
value class DefaultStepName(override val value: String) : IStepName

@Deprecated(
    message = "use StandaloneStepResult instead",
    replaceWith = ReplaceWith(
        "StandaloneStepResult<T>",
        imports = ["com.github.lemfi.kest.core.model.StandaloneStepResult"]
    )
)
typealias StepPostExecution<RESULT> = AssertableStepResult<RESULT, RESULT, RESULT>

typealias StepResult<RESULT> = IStepResult<RESULT, RESULT>
typealias StandaloneStepResult<RESULT> = AssertableStepResult<RESULT, RESULT, RESULT>
typealias NestedScenarioStepResult<RESULT> = NotAssertableStepResult<RESULT, RESULT>

@Deprecated("use IStepResult instead", replaceWith = ReplaceWith("IStepResult<RESULT, MAPPED_RESULT>"))
typealias IStepPostExecution<RESULT, MAPPED_RESULT> = IStepResult<RESULT, MAPPED_RESULT>
sealed class IStepResult<RESULT, MAPPED_RESULT>(
    private val step: Step<*>,
    private val pe: IStepResult<*, RESULT>?,
    private val transformer: (RESULT) -> MAPPED_RESULT
) {

    private var resSet = false
    private var failed: Throwable? = null
    private var res: RESULT? = null

    @Suppress("unchecked_cast")
    private val result: () -> MAPPED_RESULT = {

        val tryResolveResult: (() -> MAPPED_RESULT) -> MAPPED_RESULT = {
            try {
                it()
            } catch (e: Throwable) {
                with(
                    e orStepResultFailure StepResultResultFailure(step = step, cause = e)
                ) {
                    setFailed(this)
                    throw this
                }
            }
        }

        if (failed != null) {
            throw failed!! orStepResultFailure StepResultAssertionFailure(step = step, cause = failed!!)
        } else if (resSet) tryResolveResult {
            transformer(res as RESULT)
        } else if (pe != null) tryResolveResult {
            transformer(pe.result())
        } else throw StepResultNotPlayedFailure(step)
    }


    operator fun invoke() = result()

    @Deprecated("use future instead", replaceWith = ReplaceWith("future"))
    val lazy: () -> MAPPED_RESULT = { invoke() }
    val future: () -> MAPPED_RESULT = { invoke() }

    fun setResult(t: RESULT) {
        resSet = true
        var parent: IStepResult<*, *>? = pe
        while (parent != null) {
            parent.resSet = true
            parent = parent.pe
        }
        res = t
    }

    fun setFailed(e: Throwable) {
        failed = e
        resSet = true
        var parent: IStepResult<*, *>? = pe
        while (parent != null) {
            parent.setFailed(e)
            parent = parent.pe
        }
    }

    fun isFailed() = resSet && failed != null
    fun isSuccess() = resSet && failed == null

    fun replay() {
        resSet = false
        step.run()
    }
}

@Deprecated("use AssertableStepResult instead", replaceWith = ReplaceWith("AssertableStepResult<INITIAL_RESULT, RESULT, MAPPED_RESULT>"))
typealias StandaloneStepPostExecution<INITIAL_RESULT, RESULT, MAPPED_RESULT> = AssertableStepResult<INITIAL_RESULT, RESULT, MAPPED_RESULT>
class AssertableStepResult<INITIAL_RESULT : Any?, RESULT, MAPPED_RESULT>(
    private val step: Step<*>,
    private val pe: AssertableStepResult<INITIAL_RESULT, *, RESULT>?,
    transformer: (RESULT) -> MAPPED_RESULT
) : IStepResult<RESULT, MAPPED_RESULT>(step, pe, transformer) {

    @Deprecated("use `mapResultTo` instead", replaceWith = ReplaceWith("this mapResultTo mapper"))
    infix fun <M> `map result to`(mapper: (MAPPED_RESULT) -> M) = AssertableStepResult(step, this, mapper)
    infix fun <M> mapResultTo(mapper: (MAPPED_RESULT) -> M) = AssertableStepResult(step, this, mapper)

    val assertions: MutableList<AssertionsBuilder.(INITIAL_RESULT) -> Unit> = mutableListOf()

    fun addAssertion(assertion: AssertionsBuilder.(INITIAL_RESULT) -> Unit) {
        pe?.addAssertion(assertion) ?: assertions.add(assertion)
    }
}

@Deprecated("use NotAssertableStepResult instead", replaceWith = ReplaceWith("NotAssertableStepResult<RESULT, MAPPED_RESULT>"))
typealias NestedScenarioStepPostExecution<RESULT, MAPPED_RESULT> = NotAssertableStepResult<RESULT, MAPPED_RESULT>

class NotAssertableStepResult<RESULT, MAPPED_RESULT>(
    private val step: Step<*>,
    pe: NotAssertableStepResult<*, RESULT>?,
    transformer: (RESULT) -> MAPPED_RESULT
) : IStepResult<RESULT, MAPPED_RESULT>(step, pe, transformer) {

    @Deprecated("use `mapResultTo` instead", replaceWith = ReplaceWith("this mapResultTo mapper"))
    infix fun <M> `map result to`(mapper: (MAPPED_RESULT) -> M) = NotAssertableStepResult(step, this, mapper)
    infix fun <M> mapResultTo(mapper: (MAPPED_RESULT) -> M) = NotAssertableStepResult(step, this, mapper)

}

data class RetryStep(
    val retries: Int = 3,
    val delay: Long = 1000L,
)

val Int.times: RetryStep get() = RetryStep(retries = this)
val Int.ms: Long get() = this.toLong()
val Int.seconds: Long get() = this * 1000L
@Deprecated("use byIntervalsOf instead", replaceWith = ReplaceWith("this byIntervalsOf milliseconds"))
infix fun RetryStep.`by intervals of`(milliseconds: Long) = copy(delay = milliseconds)
infix fun RetryStep.byIntervalsOf(milliseconds: Long) = copy(delay = milliseconds)