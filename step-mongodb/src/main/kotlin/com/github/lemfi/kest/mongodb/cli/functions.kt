@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.mongodb.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.core.model.StepPostExecution
import com.github.lemfi.kest.mongodb.builder.MongoDBCleanDatabaseExecutionBuilder
import com.github.lemfi.kest.mongodb.builder.MongoDBInsertDocumentExecutionBuilder
import com.github.lemfi.kest.mongodb.builder.MongoDBUpdateDocumentExecutionBuilder

fun ScenarioBuilder.`insert mongo document`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBInsertDocumentExecutionBuilder.() -> Unit
): StepPostExecution<Unit> {
    val executionBuilder = MongoDBInsertDocumentExecutionBuilder()
    return StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("insert mongo document"),
        scenarioName = this.name,
        retry = retryStep
    )
        .addToScenario(executionBuilder, h)
}

fun ScenarioBuilder.`update mongo document`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBUpdateDocumentExecutionBuilder.() -> Unit
): StepPostExecution<Unit> {
    val executionBuilder = MongoDBUpdateDocumentExecutionBuilder()

    return StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("update mongo document"),
        scenarioName = this.name,
        retry = retryStep
    ).addToScenario(executionBuilder, h)
}


fun ScenarioBuilder.`clean mongo database`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBCleanDatabaseExecutionBuilder.() -> Unit = {}
): StepPostExecution<Unit> {
    val executionBuilder = MongoDBCleanDatabaseExecutionBuilder()
    return StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("clean database"),
        scenarioName = this.name,
        retry = retryStep
    )
        .addToScenario(executionBuilder, h)
}