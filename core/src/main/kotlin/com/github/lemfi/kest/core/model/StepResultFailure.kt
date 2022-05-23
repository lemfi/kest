package com.github.lemfi.kest.core.model


sealed class StepResultFailure(
    val step: Step<*>,
    override val message: String? = """Could not get result from previous step "${step.name?.value ?: step}"""",
    override val cause: Throwable? = null
) : Throwable(message, cause)

class StepResultAssertionFailure(
    step: Step<*>,
    override val cause: Throwable? = null
) : StepResultFailure(
    step,
    """
        
        Could not get result from previous step "${step.name?.value ?: step}"
        Assertions failed for step
        
        """.trimIndent(),
    cause
)

class StepResultResultFailure(
    step: Step<*>,
    override val cause: Throwable? = null
) : StepResultFailure(
    step,
    """
        
        Could not get result from previous step "${step.name?.value ?: step}"
        Could not compute result
        
        """.trimIndent(),
    cause
)

class StepResultNotPlayedFailure(
    step: Step<*>,
    override val cause: Throwable? = null
) : StepResultFailure(
    step,
    """
        
        Step "${step.name?.value ?: step}" was not played yet! 
        You may use its result only in another step body
        
        """.trimIndent(), cause
)

infix fun Throwable.orStepResultFailure(stepResultFailure: StepResultFailure) =
    if (this is StepResultFailure) this else stepResultFailure.also {
        it.stackTrace = stackTrace
    }