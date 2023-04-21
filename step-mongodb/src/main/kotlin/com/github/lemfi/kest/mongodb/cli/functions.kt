@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.mongodb.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.DefaultStepName
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.mongodb.builder.MongoDBCleanDatabaseExecutionBuilder
import com.github.lemfi.kest.mongodb.builder.MongoDBCountDocumentExecutionBuilder
import com.github.lemfi.kest.mongodb.builder.MongoDBDeleteDocumentExecutionBuilder
import com.github.lemfi.kest.mongodb.builder.MongoDBInsertDocumentExecutionBuilder
import com.github.lemfi.kest.mongodb.builder.MongoDBReadDocumentExecutionBuilder
import com.github.lemfi.kest.mongodb.builder.MongoDBUpdateDocumentExecutionBuilder

fun ScenarioBuilder.`insert mongo document`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBInsertDocumentExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("insert mongo document"),
        retry = retryStep
    ) { MongoDBInsertDocumentExecutionBuilder().apply(h) }

fun ScenarioBuilder.`update mongo document`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBUpdateDocumentExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("update mongo document"),
        retry = retryStep
    ) { MongoDBUpdateDocumentExecutionBuilder().apply(h) }

@Deprecated("use givenMongoDocuments instead")
fun ScenarioBuilder.`given mongo documents`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBReadDocumentExecutionBuilder.() -> Unit
) = givenMongoDocuments(name, retryStep, h)

fun ScenarioBuilder.givenMongoDocuments(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBReadDocumentExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("read mongo documents"),
        retry = retryStep
    ) { MongoDBReadDocumentExecutionBuilder().apply(h) }

@Deprecated("use deleteMongoDocuments instead")
fun ScenarioBuilder.`delete mongo documents`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBDeleteDocumentExecutionBuilder.() -> Unit
) = deleteMongoDocuments(name, retryStep, h)

fun ScenarioBuilder.deleteMongoDocuments(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBDeleteDocumentExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("delete mongo documents"),
        retry = retryStep
    ) { MongoDBDeleteDocumentExecutionBuilder().apply(h) }

@Deprecated("use givenCountOfMongoDocuments instead")
fun ScenarioBuilder.`given count of mongo documents`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBCountDocumentExecutionBuilder.() -> Unit
) = givenCountOfMongoDocuments(name, retryStep, h)

fun ScenarioBuilder.givenCountOfMongoDocuments(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBCountDocumentExecutionBuilder.() -> Unit
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("count mongo documents"),
        retry = retryStep
    ) { MongoDBCountDocumentExecutionBuilder().apply(h) }

@Deprecated("use cleanMongoDatabase instead")
fun ScenarioBuilder.`clean mongo database`(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBCleanDatabaseExecutionBuilder.() -> Unit = {}
) = cleanMongoDatabase(name, retryStep, h)

fun ScenarioBuilder.cleanMongoDatabase(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MongoDBCleanDatabaseExecutionBuilder.() -> Unit = {}
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("clean database"),
        retry = retryStep
    ) { MongoDBCleanDatabaseExecutionBuilder().apply(h) }