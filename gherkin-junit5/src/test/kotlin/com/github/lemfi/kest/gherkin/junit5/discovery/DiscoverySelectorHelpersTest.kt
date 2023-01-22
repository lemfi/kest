package com.github.lemfi.kest.gherkin.junit5.discovery

import com.github.lemfi.kest.gherkin.junit5.GherkinProp
import com.github.lemfi.kest.gherkin.junit5.KestGherkin
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots
import org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage
import org.junit.platform.engine.discovery.DiscoverySelectors.selectUri
import org.junit.platform.engine.support.descriptor.ClassSource
import java.lang.annotation.Inherited
import java.net.URI
import kotlin.io.path.Path

class DiscoverySelectorHelpersTest {

    @Test
    fun `discover feature providers classes with an unknown selector`() {
        val selector = selectUri(URI("..."))

        assertEquals(emptyList<Class<*>>(), selector.toFeatureProviderClasses())
    }

    @Test
    fun `discover feature provides classes with a class selector - ok`() {
        val selector = selectClass(FeatureClass::class.java)
        assertEquals(listOf(FeatureClass::class.java), selector.toFeatureProviderClasses())
    }

    @Test
    fun `discover feature provides classes with a class selector - ko`() {
        val selector = selectClass(NoFeatureClass::class.java)
        assertEquals(emptyList<Class<*>>(), selector.toFeatureProviderClasses().toList())
    }


    @Test
    fun `discover feature provides classes with a package selector - ok`() {
        val selector = selectPackage("com.github.lemfi.kest.gherkin.junit5.discovery")
        assertEquals(listOf(FeatureClass::class.java), selector.toFeatureProviderClasses().toList())
    }

    @Test
    fun `discover feature provides classes with a package selector - ko`() {
        val selector = selectPackage("com.github.lemfi.kest.gherkin.junit5.discovery.blah")
        assertEquals(emptyList<Class<*>>(), selector.toFeatureProviderClasses().toList())
    }

    @Test
    fun `discover feature provides classes with a ClasspathRootSelector selector - ok`() {
        val selector = selectClasspathRoots(setOf(Path("."))).first()
        assertEquals(listOf(FeatureClass::class.java), selector.toFeatureProviderClasses().toList())
    }

    @Test
    fun `discover feature providers classes with a list of class selectors`() {

        val selectors = listOf(selectClass(NoFeatureClass::class.java), selectClass(FeatureClass::class.java))

        assertEquals(listOf(FeatureClass::class.java), selectors.toClasses().toList())
    }

    @Test
    fun `discover feature providers classes with a list of package selectors`() {

        val selectors = listOf(selectPackage("com.github.lemfi.kest.gherkin.junit5.discovery"), selectPackage("com.github.lemfi.kest.gherkin.junit5.discovery.blah"))

        assertEquals(listOf(FeatureClass::class.java), selectors.toClasses().toList())
    }

    @Test
    fun `discover configuration from an annotated class - ok`() {

        mockkStatic(KestGherkin::toFeaturesDiscoveryConfiguration) {
            val conf = listOf(
                FeaturesDiscoveryConfiguration(
                    features = listOf("feature1", "feature2"),
                    stepsPackages = listOf("stepPackage1", "stepPackage2"),
                    source = ClassSource.from(FeatureClass::class.java)
                )
            )

            every { KestGherkin::toFeaturesDiscoveryConfiguration.invoke(any(), any()) } returns conf

            val res = FeatureClass::class.java.toFeaturesDiscoveryConfiguration()

            verify { KestGherkin::toFeaturesDiscoveryConfiguration.invoke(any(), any()) }

            assertEquals(conf, res)
        }

    }

    @Test
    fun `discover configuration from an annotated class - no tests found`() {

        mockkStatic(KestGherkin::toFeaturesDiscoveryConfiguration) {

            every { KestGherkin::toFeaturesDiscoveryConfiguration.invoke(any(), any()) } returns emptyList()

            val res = NoFeatureClass::class.java.toFeaturesDiscoveryConfiguration()

            verify(exactly = 0) { KestGherkin::toFeaturesDiscoveryConfiguration.invoke(any(), any()) }

            assertEquals(emptyList<FeaturesDiscoveryConfiguration>(), res)
        }
    }

    @Test
    fun `convert KestGherkin annotation to FeaturesDiscoveryConfiguration - default values`() {

        mockkConstructor(GherkinProp::class) {

            every { anyConstructed<GherkinProp>().getProperty("stepDefinitions") } returns listOf(
                "packageFromConf1",
                "packageFromConf2"
            )

            mockkStatic(::resourceSources) {

                every { resourceSources(any()) } returns listOf("feature1", "feature2")

                val res = KestGherkin().toFeaturesDiscoveryConfiguration(Any::class.java)

                verify { resourceSources("/gherkin") }

                assertEquals(
                    listOf(
                        FeaturesDiscoveryConfiguration(
                            features = listOf("feature1", "feature2"),
                            stepsPackages = listOf("packageFromConf1", "packageFromConf2"),
                            source = ClassSource.from(Any::class.java)
                        )
                    ), res
                )
            }

        }
    }

    @Test
    fun `convert KestGherkin annotation to FeaturesDiscoveryConfiguration - custom values`() {

        mockkStatic(::resourceSources) {

            every { resourceSources(any()) } returns listOf("feature1", "feature2")

            val res = KestGherkin(
                path = "/custompath",
                stepDefinitionsPackage = listOf("packageFromConf1", "packageFromConf2").toTypedArray(),
            ).toFeaturesDiscoveryConfiguration(Any::class.java)

            verify { resourceSources("/custompath") }

            assertEquals(
                listOf(
                    FeaturesDiscoveryConfiguration(
                        features = listOf("feature1", "feature2"),
                        stepsPackages = listOf("packageFromConf1", "packageFromConf2"),
                        source = ClassSource.from(Any::class.java)
                    )
                ),
                res
            )
        }
    }

    @Test
    fun `convert not supported annotation to FeaturesDiscoveryConfiguration throws an exception`() {


        val exception = assertThrows<IllegalArgumentException> {
            Inherited().toFeaturesDiscoveryConfiguration(Any::class.java)
        }

        assertEquals("@java.lang.annotation.Inherited() is not a recognized source provider", exception.message)
    }

    @Test
    fun `read features files from resources`() {

        val resources = resourceSources("/discoveryhelpersgherkintest")

        assertEquals(1, resources.size)
        assertEquals("""Feature: THIS IS A GHERKIN FILE

  Scenario: hello world
    Given someone
    When he says Hello
    Then you answer world""", resources.first())

    }

    @Test
    fun `read features files from unknown folder`() {

        val resources = resourceSources("/blah")

        assertTrue(resources.isEmpty())

    }
}

@KestGherkin
private class FeatureClass
private class NoFeatureClass