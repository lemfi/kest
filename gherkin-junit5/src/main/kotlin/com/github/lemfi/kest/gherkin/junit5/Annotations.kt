@file:Suppress("unused")

package com.github.lemfi.kest.gherkin.junit5

import com.github.lemfi.kest.gherkin.junit5.discovery.KestGherkinFeaturesProvider
import org.junit.platform.commons.annotation.Testable
import kotlin.reflect.KClass

@Testable
annotation class KestGherkin(
    val path: String = "/gherkin",
    vararg val stepDefinitionsPackage: String = [],
)

@Testable
annotation class KestGherkinCustom(
    val sourceProvider: KClass<out KestGherkinFeaturesProvider>,
    vararg val stepDefinitionsPackage: String = [],
)