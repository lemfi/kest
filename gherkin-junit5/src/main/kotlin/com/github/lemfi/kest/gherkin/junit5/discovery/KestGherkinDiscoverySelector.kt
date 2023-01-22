package com.github.lemfi.kest.gherkin.junit5.discovery

import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.TestSource

interface KestGherkinFeaturesProvider {

    fun getFeatures(): List<String>

}

internal interface KestGherkinFilterableFeaturesProvider : KestGherkinFeaturesProvider {

    fun filters(): List<String> = emptyList()
}

internal fun KestGherkinFeaturesProvider.toFeaturesDiscoveryConfiguration(
    stepsDefinitionPackages: Collection<String>,
    source: TestSource,
): List<FeaturesDiscoveryConfiguration> = listOf(
    FeaturesDiscoveryConfiguration(
        features = getFeatures(),
        stepsPackages = stepsDefinitionPackages,
        source = source,
        filter = if (this is KestGherkinFilterableFeaturesProvider) filters() else emptyList(),
    )
)

internal interface KestGherkinDiscoverySelector : DiscoverySelector, KestGherkinFeaturesProvider {
    fun getStepDefinitionsPackages(): Collection<String>
}