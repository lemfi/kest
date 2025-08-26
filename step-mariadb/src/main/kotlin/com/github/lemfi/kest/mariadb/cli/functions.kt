package com.github.lemfi.kest.mariadb.cli

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.model.DefaultStepName
import com.github.lemfi.kest.core.model.RetryStep
import com.github.lemfi.kest.core.model.StepName
import com.github.lemfi.kest.mariadb.builder.MariaDBCleanDatabaseExecutionBuilder
import com.github.lemfi.kest.mariadb.builder.MariaDBReadDatabaseExecutionBuilder
import com.github.lemfi.kest.mariadb.builder.MariaDBUpdateOperationDatabaseExecutionBuilder


fun ScenarioBuilder.cleanMariaDBDatabase(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MariaDBCleanDatabaseExecutionBuilder.() -> Unit = {}
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("clean database"),
        retry = retryStep
    ) { MariaDBCleanDatabaseExecutionBuilder().apply(h) }


fun ScenarioBuilder.readMariaDBDatabase(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MariaDBReadDatabaseExecutionBuilder.() -> Unit = {}
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("read database"),
        retry = retryStep
    ) { MariaDBReadDatabaseExecutionBuilder().apply(h) }


fun ScenarioBuilder.updateMariaDBDatabase(
    name: String? = null,
    retryStep: RetryStep? = null,
    h: MariaDBUpdateOperationDatabaseExecutionBuilder.() -> Unit = {}
) =

    createStep(
        name = name?.let { StepName(it) } ?: DefaultStepName("update database"),
        retry = retryStep
    ) { MariaDBUpdateOperationDatabaseExecutionBuilder().apply(h) }