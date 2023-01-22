package com.github.lemfi.kest.gherkin.junit5.discovery

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.ClasspathRootSelector
import org.junit.platform.engine.discovery.DirectorySelector
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots
import org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage
import org.junit.platform.engine.discovery.DiscoverySelectors.selectUri
import org.junit.platform.engine.discovery.FileSelector
import org.junit.platform.engine.discovery.PackageSelector
import java.net.URI
import kotlin.io.path.Path

class KestGherkinFilterDiscoverySelectorTest {

    @Test
    fun `features are extracted from Class selectors`() {

        mockkStatic(Collection<ClassSelector>::toFeaturesDiscoveryConfiguration) {

            val classSelector = selectClass(FeatureClass::class.java)

            every { listOf(classSelector).toFeaturesDiscoveryConfiguration() } returns listOf(
                FeaturesDiscoveryConfiguration(
                    features = listOf("f1", "f2"),
                    source = object : TestSource {}
                )
            )

            val selector = KestGherkinFilterDiscoverySelector(
                ids = emptyList(),
                classSelector
            )

            val res = selector.getFeatures()

            assertEquals(listOf("f1", "f2"), res)
        }
    }

    @Test
    fun `features are extracted from ClasspathRoot selectors`() {

        mockkStatic(Collection<ClasspathRootSelector>::toFeaturesDiscoveryConfiguration) {

            val classpathRootSelector = selectClasspathRoots(setOf(Path(".")))

            every { classpathRootSelector.toFeaturesDiscoveryConfiguration() } returns listOf(
                FeaturesDiscoveryConfiguration(
                    features = listOf("f1", "f2"),
                    source = object : TestSource {}
                )
            )

            val selector = KestGherkinFilterDiscoverySelector(
                ids = emptyList(),
                *classpathRootSelector.toTypedArray()
            )

            val res = selector.getFeatures()

            assertEquals(listOf("f1", "f2"), res)
        }
    }

    @Test
    fun `features are extracted from Package selectors`() {

        mockkStatic(Collection<PackageSelector>::toFeaturesDiscoveryConfiguration) {

            val packageSelector = selectPackage("com.github.lemfi")

            every { listOf(packageSelector).toFeaturesDiscoveryConfiguration() } returns listOf(
                FeaturesDiscoveryConfiguration(
                    features = listOf("f1", "f2"),
                    source = object : TestSource {}
                )
            )

            val selector = KestGherkinFilterDiscoverySelector(
                ids = emptyList(),
                packageSelector
            )

            val res = selector.getFeatures()

            assertEquals(listOf("f1", "f2"), res)
        }
    }

    @Test
    fun `features are extracted from File selectors`() {

        mockkStatic(Collection<FileSelector>::toFeaturesDiscoveryConfiguration) {

            val fileSelector = mockk<FileSelector>()

            every { listOf(fileSelector).toFeaturesDiscoveryConfiguration() } returns listOf(
                FeaturesDiscoveryConfiguration(
                    features = listOf("f1", "f2"),
                    source = object : TestSource {}
                )
            )

            val selector = KestGherkinFilterDiscoverySelector(
                ids = emptyList(),
                fileSelector
            )

            val res = selector.getFeatures()

            assertEquals(listOf("f1", "f2"), res)
        }
    }

    @Test
    fun `features are extracted from Directory selectors`() {

        mockkStatic(Collection<DirectorySelector>::toFeaturesDiscoveryConfiguration) {

            val directorySelector = mockk<DirectorySelector>()

            every { listOf(directorySelector).toFeaturesDiscoveryConfiguration() } returns listOf(
                FeaturesDiscoveryConfiguration(
                    features = listOf("f1", "f2"),
                    source = object : TestSource {}
                )
            )

            val selector = KestGherkinFilterDiscoverySelector(
                ids = emptyList(),
                directorySelector
            )

            val res = selector.getFeatures()

            assertEquals(listOf("f1", "f2"), res)
        }
    }

    @Test
    fun `features are extracted from mixed selectors`() {

        mockkStatic(
            Collection<DirectorySelector>::toFeaturesDiscoveryConfiguration,
            Collection<FileSelector>::toFeaturesDiscoveryConfiguration,
            Collection<ClassSelector>::toFeaturesDiscoveryConfiguration,
            Collection<ClasspathRootSelector>::toFeaturesDiscoveryConfiguration,
            Collection<PackageSelector>::toFeaturesDiscoveryConfiguration
        ) {

            val directorySelector = mockk<DirectorySelector>()

            every { listOf(directorySelector).toFeaturesDiscoveryConfiguration() } returns listOf(
                FeaturesDiscoveryConfiguration(
                    features = listOf("f1", "f2"),
                    source = object : TestSource {}
                )
            )
            val fileSelector = mockk<FileSelector>()

            every { listOf(fileSelector).toFeaturesDiscoveryConfiguration() } returns listOf(
                FeaturesDiscoveryConfiguration(
                    features = listOf("f3", "f4"),
                    source = object : TestSource {}
                )
            )

            val packageSelector = selectPackage("com.github.lemfi")

            every { listOf(packageSelector).toFeaturesDiscoveryConfiguration() } returns listOf(
                FeaturesDiscoveryConfiguration(
                    features = listOf("f5", "f6"),
                    source = object : TestSource {}
                )
            )

            val classpathRootSelector = selectClasspathRoots(setOf(Path(".")))

            every { classpathRootSelector.toFeaturesDiscoveryConfiguration() } returns listOf(
                FeaturesDiscoveryConfiguration(
                    features = listOf("f7", "f8"),
                    source = object : TestSource {}
                )
            )

            val classSelector = selectClass(FeatureClass::class.java)

            every { listOf(classSelector).toFeaturesDiscoveryConfiguration() } returns listOf(
                FeaturesDiscoveryConfiguration(
                    features = listOf("f9", "f10"),
                    source = object : TestSource {}
                )
            )

            val selector = KestGherkinFilterDiscoverySelector(
                ids = emptyList(),
                directorySelector, fileSelector, packageSelector, classSelector, *classpathRootSelector.toTypedArray()
            )

            val res = selector.getFeatures()

            assertEquals(setOf("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10"), res.toSet())
        }
    }

    @Test
    fun `unsupported selectors are ignored`() {

        val selector = KestGherkinFilterDiscoverySelector(
            ids = emptyList(),
            selectUri(URI("."))
        )

        val res = selector.getFeatures()

        assertEquals(emptyList<String>(), res)
    }
}