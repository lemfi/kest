package com.github.lemfi.kest.gherkin.junit5.discovery

import com.github.lemfi.kest.gherkin.junit5.KestGherkin
import com.github.lemfi.kest.gherkin.junit5.KestGherkinCustom


@KestGherkin
class FeatureClass
class NoFeatureClass

@KestGherkinCustom(sourceProvider = Provider::class)
class FeatureClassFromCustomProvider

class Provider: KestGherkinFeaturesProvider {

    override fun getFeatures(): List<String> = listOf("Feature: feature1", "Feature: feature2")

}