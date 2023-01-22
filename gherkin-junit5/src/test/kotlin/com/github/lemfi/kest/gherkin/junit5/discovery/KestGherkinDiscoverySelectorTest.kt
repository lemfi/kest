package com.github.lemfi.kest.gherkin.junit5.discovery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.platform.engine.TestSource

class KestGherkinDiscoverySelectorTest {

    @Test
    fun `features discovery configuration is built from KestGherkinFeaturesProvider`() {

        val provider = object : KestGherkinFeaturesProvider {
            override fun getFeatures(): List<String> = listOf("f")
        }

        val source = object : TestSource {}

        val res = provider.toFeaturesDiscoveryConfiguration(source = source, stepsDefinitionPackages = listOf("p"))

        assertEquals(
            listOf(FeaturesDiscoveryConfiguration(
                source = source,
                stepsPackages = listOf("p"),
                features = listOf("f")
            )),
            res
        )

    }

    @Test
    fun `features discovery configuration is built from KestGherkinFilterableFeaturesProvider`() {

        val provider = object : KestGherkinFilterableFeaturesProvider {
            override fun getFeatures(): List<String> = listOf("f")

            override fun filters(): List<String> = listOf("filters")
        }

        val source = object : TestSource {}

        val res = provider.toFeaturesDiscoveryConfiguration(source = source, stepsDefinitionPackages = listOf("p"))

        assertEquals(
            listOf(FeaturesDiscoveryConfiguration(
                source = source,
                stepsPackages = listOf("p"),
                features = listOf("f"),
                filter = listOf("filters")
            )),
            res
        )

    }
}