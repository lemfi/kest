@file:Suppress("unused")

package com.github.lemfi.kest.gherkin.core

annotation class Gherkin(
    val label: String
)

@Gherkin("Given ")
annotation class Given(
    val sentence: String,
)

@Gherkin("When ")
annotation class When(
    val sentence: String,
)

@Gherkin("Then ")
annotation class Then(
    val sentence: String,
)

@Gherkin("But ")
annotation class But(
    val sentence: String,
)

@Gherkin("And ")
annotation class And(
    val sentence: String,
)