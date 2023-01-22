package com.github.lemfi.kest.gherkin.junit5.discovery

import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.ClasspathRootSelector
import org.junit.platform.engine.discovery.DirectorySelector
import org.junit.platform.engine.discovery.FileSelector
import org.junit.platform.engine.discovery.PackageSelector
import org.slf4j.LoggerFactory

@Suppress("unused")
class KestGherkinFilterDiscoverySelector(
    private val ids: List<String>,
    private vararg val selectors: DiscoverySelector
) : KestGherkinDiscoverySelector, KestGherkinFilterableFeaturesProvider {

    private val features: List<String>
    private val stepsPackages: Collection<String>

    init {
        val notSupported = selectors.filter { it !is DirectorySelector && it !is FileSelector && it !is ClassSelector && it !is ClasspathRootSelector && it !is PackageSelector }

        if (notSupported.isNotEmpty()) {
            LoggerFactory.getLogger(this::class.java).warn("unsupported selectors: ${notSupported.map { it.javaClass.simpleName }} for KestGherkinFilterDiscoverySelector, they will be ignored")
        }

        val configuration = selectors
            .filterIsInstance<ClassSelector>()
            .toFeaturesDiscoveryConfiguration() +
                selectors
                    .filterIsInstance<ClasspathRootSelector>()
                    .toFeaturesDiscoveryConfiguration() +
                selectors
                    .filterIsInstance<PackageSelector>()
                    .toFeaturesDiscoveryConfiguration() +
                selectors
                    .filterIsInstance<FileSelector>()
                    .toFeaturesDiscoveryConfiguration() +
                selectors
                    .filterIsInstance<DirectorySelector>()
                    .toFeaturesDiscoveryConfiguration()

        features = configuration.flatMap { it.features }
        stepsPackages = configuration.flatMap { it.stepsPackages }.toSet()
    }

    constructor(
        id: String,
        vararg selectors: DiscoverySelector
    ) : this(listOf(id), *selectors)

    override fun filters() = ids

    override fun getFeatures(): List<String> = features

    override fun getStepDefinitionsPackages() = stepsPackages
}