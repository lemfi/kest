@file:Suppress("unused")

package com.github.lemfi.kest.gherkin.junit5

import org.junit.platform.commons.annotation.Testable

@Testable
annotation class KestGherkin(
    val path: String = "/gherkin",
    vararg val stepDefinitionsPackage: String = [],
)