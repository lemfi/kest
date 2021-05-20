package com.github.lemfi.kest.core.model

import com.github.lemfi.kest.core.builder.AssertionsBuilder

data class Step<T>(
        val execution: () -> Execution<T>,
        val retry: RetryStep?,

        ) {
        var postExecution: StepPostExecution<T> = StepPostExecution { t -> t }
}

typealias StepPostExecution<T> = IStepPostExecution<T, T, T>

class IStepPostExecution<I, T, R>(private val pe: IStepPostExecution<I, *, T>? = null, private val transformer: (T)->R) {
        private var resSet = false
        private var res: T? = null

        val result: ()->R = {
                if (resSet) transformer(res as T)
                else if (pe != null) transformer(pe.result())
                else throw IllegalAccessException("Step not played yet!")
        }

        infix fun <M> `map result to`(l: (R)->M): IStepPostExecution<I, R, M> = IStepPostExecution(this, l)

        fun setResult(t: T) {
                resSet = true
                res = t
        }
        val assertions: MutableList<AssertionsBuilder.(I)->Unit> = mutableListOf()

        fun addAssertion(assertion: AssertionsBuilder.(I)->Unit) {
                pe?.addAssertion(assertion) ?: assertions.add(assertion)
        }
}

data class RetryStep(
        val retries: Int = 3,
        val delay: Long = 1000L,
)