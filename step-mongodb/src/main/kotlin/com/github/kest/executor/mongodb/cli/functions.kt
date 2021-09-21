package com.github.kest.executor.mongodb.cli

import com.github.kest.executor.mongodb.builder.MongoDBCleanDatabaseExecutionBuilder
import com.github.kest.executor.mongodb.builder.MongoDBInsertDocumentExecutionBuilder
import com.github.kest.executor.mongodb.builder.MongoDBUpdateDocumentExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StandaloneStep
import com.github.lemfi.kest.core.model.StepName

fun ScenarioBuilder.`insert mongo document`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBInsertDocumentExecutionBuilder.() -> Unit
) {
    val executionBuilder = MongoDBInsertDocumentExecutionBuilder()
    StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("insert mongo document"),
        scenarioName = this.name!!,
        retry = retryStep
    )
        .addToScenario(executionBuilder, h)
}

fun ScenarioBuilder.`update mongo document`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBUpdateDocumentExecutionBuilder.() -> Unit
) {
    val executionBuilder = MongoDBUpdateDocumentExecutionBuilder()

    StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("update mongo document"),
        scenarioName = this.name!!,
        retry = retryStep
    ).addToScenario(executionBuilder, h)
}


fun ScenarioBuilder.`clean mongo database`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBCleanDatabaseExecutionBuilder.() -> Unit = {}
) {
    val executionBuilder = MongoDBCleanDatabaseExecutionBuilder()
    StandaloneStep<Unit>(
        name = name?.let { StepName(it) } ?: StepName("clean database"),
        scenarioName = this.name!!,
        retry = retryStep
    )
        .addToScenario(executionBuilder, h)
}