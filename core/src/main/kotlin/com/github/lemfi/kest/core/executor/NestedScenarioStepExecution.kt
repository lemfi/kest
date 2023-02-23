package com.github.lemfi.kest.core.executor

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.NestedScenario
import com.github.lemfi.kest.core.model.Step

class NestedScenarioStepExecution<RESULT>(
    val step: Step<RESULT>,
    val scenario: () -> NestedScenario<RESULT>,
) : Execution<RESULT>() {

    override fun execute(): RESULT =

        scenario()
            .run {
                run()
                resolve()
            }
}
