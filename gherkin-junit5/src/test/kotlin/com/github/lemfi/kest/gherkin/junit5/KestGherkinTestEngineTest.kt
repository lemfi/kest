package com.github.lemfi.kest.gherkin.junit5

import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.cli.nestedScenario
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.core.cli.step
import com.github.lemfi.kest.core.model.NestedScenarioStep
import com.github.lemfi.kest.gherkin.core.GherkinScenarioBuilder
import com.github.lemfi.kest.gherkin.junit5.discovery.FeaturesDiscoveryConfiguration
import com.github.lemfi.kest.gherkin.junit5.discovery.KestGherkinDiscoverySelector
import com.github.lemfi.kest.gherkin.junit5.discovery.KestGherkinFilterDiscoverySelector
import com.github.lemfi.kest.gherkin.junit5.discovery.toFeaturesDiscoveryConfiguration
import io.mockk.EqMatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.platform.engine.ConfigurationParameters
import org.junit.platform.engine.DiscoveryFilter
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.ClasspathRootSelector
import org.junit.platform.engine.discovery.DirectorySelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory
import org.junit.platform.engine.discovery.DiscoverySelectors.selectFile
import org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage
import org.junit.platform.engine.discovery.FileSelector
import org.junit.platform.engine.discovery.PackageSelector
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource
import org.opentest4j.AssertionFailedError
import java.io.File
import kotlin.io.path.Path

class KestGherkinTestEngineTest {

    @Test
    fun `kest gherkin test engines discovers tests from ClassSelector`() {

        mockkConstructor(GherkinScenarioBuilder::class) {

            mockkStatic(
                Collection<ClassSelector>::toFeaturesDiscoveryConfiguration,
                GherkinScenarioBuilder::resourceToScenarios
            ) {

                val request = object : EngineDiscoveryRequest {
                    override fun <T : DiscoverySelector> getSelectorsByType(cls: Class<T>): MutableList<T> =
                        if (cls == ClassSelector::class.java) {
                            @Suppress("UNCHECKED_CAST")
                            mutableListOf<ClassSelector>(DiscoverySelectors.selectClass(FeatureClass::class.java)) as MutableList<T>
                        } else mutableListOf()

                    override fun <T : DiscoveryFilter<*>?> getFiltersByType(p0: Class<T>?): MutableList<T> {
                        throw NotImplementedError("...")
                    }

                    override fun getConfigurationParameters(): ConfigurationParameters {
                        throw NotImplementedError("...")
                    }
                }

                val fdc = listOf(
                    FeaturesDiscoveryConfiguration(
                        features = listOf("f"),
                        stepsPackages = listOf("s"),
                        source = ClassSource.from(FeatureClass::class.java)
                    )
                )

                every { Collection<ClassSelector>::toFeaturesDiscoveryConfiguration.invoke(any()) } returns fdc

                val s1 =
                    scenario(name = "s1") { nestedScenario(name = "scenario1") { }; nestedScenario(name = "scenario2") { } }
                val s2 =
                    scenario(name = "s2") { nestedScenario(name = "scenario3") { }; nestedScenario(name = "scenario4") { } }
                val scenarios = listOf(s1, s2)

                every { anyConstructed<GherkinScenarioBuilder>().resourceToScenarios(any()) } returns scenarios

                val res = KestGherkinTestEngine().discover(request, UniqueId.forEngine(KestGherkinTestEngineName))

                assertTrue(res.isRoot)
                assertEquals(2, res.children.size)
                assertTrue(res.children.all { it is FeatureTestDescriptor })
                assertEquals("s1", res.children.first().displayName)
                assertEquals("s2", res.children.last().displayName)
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s1]", res.children.first().uniqueId.toString())
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s2]", res.children.last().uniqueId.toString())

                assertEquals(2, res.children.first().children.size)
                assertTrue(res.children.first().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario1", res.children.first().children.first().displayName)
                assertEquals("scenario2", res.children.first().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario1]",
                    res.children.first().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario2]",
                    res.children.first().children.last().uniqueId.toString()
                )

                assertEquals(2, res.children.last().children.size)
                assertTrue(res.children.last().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario3", res.children.last().children.first().displayName)
                assertEquals("scenario4", res.children.last().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario3]",
                    res.children.last().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario4]",
                    res.children.last().children.last().uniqueId.toString()
                )

            }

        }
    }

    @Test
    fun `kest gherkin test engines discovers tests from ClasspathRootSelector`() {

        mockkConstructor(GherkinScenarioBuilder::class) {

            mockkStatic(
                Collection<ClasspathRootSelector>::toFeaturesDiscoveryConfiguration,
                GherkinScenarioBuilder::resourceToScenarios
            ) {

                val request = object : EngineDiscoveryRequest {
                    override fun <T : DiscoverySelector> getSelectorsByType(cls: Class<T>): MutableList<T> =
                        if (cls == ClasspathRootSelector::class.java) {
                            @Suppress("UNCHECKED_CAST")
                            DiscoverySelectors.selectClasspathRoots(mutableSetOf(Path("."))) as MutableList<T>
                        } else mutableListOf()

                    override fun <T : DiscoveryFilter<*>?> getFiltersByType(p0: Class<T>?): MutableList<T> {
                        throw NotImplementedError("...")
                    }

                    override fun getConfigurationParameters(): ConfigurationParameters {
                        throw NotImplementedError("...")
                    }
                }

                val fdc = listOf(
                    FeaturesDiscoveryConfiguration(
                        features = listOf("f"),
                        stepsPackages = listOf("s"),
                        source = ClasspathResourceSource.from("...")
                    )
                )

                every { Collection<ClasspathRootSelector>::toFeaturesDiscoveryConfiguration.invoke(any()) } returns fdc

                val s1 =
                    scenario(name = "s1") { nestedScenario(name = "scenario1") { }; nestedScenario(name = "scenario2") { } }
                val s2 =
                    scenario(name = "s2") { nestedScenario(name = "scenario3") { }; nestedScenario(name = "scenario4") { } }
                val scenarios = listOf(s1, s2)

                every { constructedWith<GherkinScenarioBuilder>(EqMatcher(listOf("s"))).resourceToScenarios(listOf("f")) } returns scenarios

                val res = KestGherkinTestEngine().discover(request, UniqueId.forEngine(KestGherkinTestEngineName))

                verify { constructedWith<GherkinScenarioBuilder>(EqMatcher(listOf("s"))).resourceToScenarios(listOf("f")) }

                assertTrue(res.isRoot)
                assertEquals(2, res.children.size)
                assertTrue(res.children.all { it is FeatureTestDescriptor })
                assertEquals("s1", res.children.first().displayName)
                assertEquals("s2", res.children.last().displayName)
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s1]", res.children.first().uniqueId.toString())
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s2]", res.children.last().uniqueId.toString())

                assertEquals(2, res.children.first().children.size)
                assertTrue(res.children.first().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario1", res.children.first().children.first().displayName)
                assertEquals("scenario2", res.children.first().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario1]",
                    res.children.first().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario2]",
                    res.children.first().children.last().uniqueId.toString()
                )

                assertEquals(2, res.children.last().children.size)
                assertTrue(res.children.last().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario3", res.children.last().children.first().displayName)
                assertEquals("scenario4", res.children.last().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario3]",
                    res.children.last().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario4]",
                    res.children.last().children.last().uniqueId.toString()
                )

            }

        }
    }

    @Test
    fun `kest gherkin test engines discovers tests from PackageSelector`() {

        mockkConstructor(GherkinScenarioBuilder::class) {

            mockkStatic(
                GherkinScenarioBuilder::resourceToScenarios,
                Collection<PackageSelector>::toFeaturesDiscoveryConfiguration
            ) {

                val request = object : EngineDiscoveryRequest {
                    override fun <T : DiscoverySelector> getSelectorsByType(cls: Class<T>): MutableList<T> =
                        if (cls == PackageSelector::class.java) {
                            @Suppress("UNCHECKED_CAST")
                            mutableListOf(selectPackage("com.github.lemfi.kest.gherkin.junit5")) as MutableList<T>
                        } else mutableListOf()

                    override fun <T : DiscoveryFilter<*>?> getFiltersByType(p0: Class<T>?): MutableList<T> {
                        throw NotImplementedError("...")
                    }

                    override fun getConfigurationParameters(): ConfigurationParameters {
                        throw NotImplementedError("...")
                    }
                }

                val fdc = listOf(
                    FeaturesDiscoveryConfiguration(
                        features = listOf("f"),
                        stepsPackages = listOf("s"),
                        source = ClasspathResourceSource.from("...")
                    )
                )

                every { Collection<PackageSelector>::toFeaturesDiscoveryConfiguration.invoke(any()) } returns fdc

                val s1 =
                    scenario(name = "s1") { nestedScenario(name = "scenario1") { }; nestedScenario(name = "scenario2") { } }
                val s2 =
                    scenario(name = "s2") { nestedScenario(name = "scenario3") { }; nestedScenario(name = "scenario4") { } }
                val scenarios = listOf(s1, s2)

                every { constructedWith<GherkinScenarioBuilder>(EqMatcher(listOf("s"))).resourceToScenarios(listOf("f")) } returns scenarios

                val res = KestGherkinTestEngine().discover(request, UniqueId.forEngine(KestGherkinTestEngineName))

                verify { constructedWith<GherkinScenarioBuilder>(EqMatcher(listOf("s"))).resourceToScenarios(listOf("f")) }

                assertTrue(res.isRoot)
                assertEquals(2, res.children.size)
                assertTrue(res.children.all { it is FeatureTestDescriptor })
                assertEquals("s1", res.children.first().displayName)
                assertEquals("s2", res.children.last().displayName)
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s1]", res.children.first().uniqueId.toString())
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s2]", res.children.last().uniqueId.toString())

                assertEquals(2, res.children.first().children.size)
                assertTrue(res.children.first().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario1", res.children.first().children.first().displayName)
                assertEquals("scenario2", res.children.first().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario1]",
                    res.children.first().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario2]",
                    res.children.first().children.last().uniqueId.toString()
                )

                assertEquals(2, res.children.last().children.size)
                assertTrue(res.children.last().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario3", res.children.last().children.first().displayName)
                assertEquals("scenario4", res.children.last().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario3]",
                    res.children.last().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario4]",
                    res.children.last().children.last().uniqueId.toString()
                )

            }

        }
    }

    @Test
    fun `kest gherkin test engines discovers tests from FileSelector`() {

        mockkConstructor(GherkinScenarioBuilder::class, GherkinProp::class) {

            every { anyConstructed<GherkinProp>().getProperty("stepDefinitions") } returns listOf(
                "packageFromConf1",
                "packageFromConf2"
            )

            mockkStatic(
                GherkinScenarioBuilder::resourceToScenarios,
                File::readText
            ) {

                val request = object : EngineDiscoveryRequest {
                    override fun <T : DiscoverySelector> getSelectorsByType(cls: Class<T>): MutableList<T> =
                        if (cls == FileSelector::class.java) {
                            val file = mockk<File>()
                            every { file.isFile } returns true
                            every { file.canonicalPath } returns "/file"
                            every { file.readText(any()) } returns "file content"
                            every { file.path } returns "/file"
                            @Suppress("UNCHECKED_CAST")
                            mutableListOf(selectFile(file)) as MutableList<T>
                        } else mutableListOf()

                    override fun <T : DiscoveryFilter<*>?> getFiltersByType(p0: Class<T>?): MutableList<T> {
                        throw NotImplementedError("...")
                    }

                    override fun getConfigurationParameters(): ConfigurationParameters {
                        throw NotImplementedError("...")
                    }
                }

                val s1 =
                    scenario(name = "s1") { nestedScenario(name = "scenario1") { }; nestedScenario(name = "scenario2") { } }
                val s2 =
                    scenario(name = "s2") { nestedScenario(name = "scenario3") { }; nestedScenario(name = "scenario4") { } }
                val scenarios = listOf(s1, s2)

                every {
                    constructedWith<GherkinScenarioBuilder>(
                        EqMatcher(
                            listOf(
                                "packageFromConf1",
                                "packageFromConf2"
                            )
                        )
                    ).resourceToScenarios(any())
                } returns scenarios

                val res = KestGherkinTestEngine().discover(request, UniqueId.forEngine(KestGherkinTestEngineName))

                verify {
                    constructedWith<GherkinScenarioBuilder>(
                        EqMatcher(
                            listOf(
                                "packageFromConf1",
                                "packageFromConf2"
                            )
                        )
                    ).resourceToScenarios(listOf("file content"))
                }


                assertTrue(res.isRoot)
                assertEquals(2, res.children.size)
                assertTrue(res.children.all { it is FeatureTestDescriptor })
                assertEquals("s1", res.children.first().displayName)
                assertEquals("s2", res.children.last().displayName)
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s1]", res.children.first().uniqueId.toString())
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s2]", res.children.last().uniqueId.toString())

                assertEquals(2, res.children.first().children.size)
                assertTrue(res.children.first().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario1", res.children.first().children.first().displayName)
                assertEquals("scenario2", res.children.first().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario1]",
                    res.children.first().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario2]",
                    res.children.first().children.last().uniqueId.toString()
                )

                assertEquals(2, res.children.last().children.size)
                assertTrue(res.children.last().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario3", res.children.last().children.first().displayName)
                assertEquals("scenario4", res.children.last().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario3]",
                    res.children.last().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario4]",
                    res.children.last().children.last().uniqueId.toString()
                )


            }

        }
    }

    @Test
    fun `kest gherkin test engines discovers tests from DirectorySelector`() {

        mockkConstructor(GherkinScenarioBuilder::class, GherkinProp::class) {

            every { anyConstructed<GherkinProp>().getProperty("stepDefinitions") } returns listOf(
                "packageFromConf1",
                "packageFromConf2"
            )

            mockkStatic(
                GherkinScenarioBuilder::resourceToScenarios,
                File::readText,
                File::walkTopDown
            ) {

                val request = object : EngineDiscoveryRequest {
                    override fun <T : DiscoverySelector> getSelectorsByType(cls: Class<T>): MutableList<T> =
                        if (cls == DirectorySelector::class.java) {
                            val directory = mockk<File>()
                            every { directory.isDirectory } returns true
                            every { directory.canonicalPath } returns "/dir"
                            every { directory.canonicalFile } returns File("/dir")
                            every { directory.path } returns "/dir"

                            val fileTreeWalk = mockk<FileTreeWalk>()
                            every { directory.walkTopDown() } returns fileTreeWalk

                            val f1 = mockk<File>()
                            every { f1.path } returns "/file1"
                            every { f1.isDirectory } returns false
                            every { f1.readText(any()) } returns "file content 1"

                            val f2 = mockk<File>()
                            every { f2.path } returns "/file2"
                            every { f2.isDirectory } returns false
                            every { f2.readText(any()) } returns "file content 2"

                            every { fileTreeWalk.iterator() } returns listOf(f1, f2).iterator()
                            @Suppress("UNCHECKED_CAST")
                            mutableListOf(selectDirectory(directory)) as MutableList<T>
                        } else mutableListOf()

                    override fun <T : DiscoveryFilter<*>?> getFiltersByType(p0: Class<T>?): MutableList<T> {
                        throw NotImplementedError("...")
                    }

                    override fun getConfigurationParameters(): ConfigurationParameters {
                        throw NotImplementedError("...")
                    }
                }

                val s1 =
                    scenario(name = "s1") { nestedScenario(name = "scenario1") { }; nestedScenario(name = "scenario2") { } }
                val s2 =
                    scenario(name = "s2") { nestedScenario(name = "scenario3") { }; nestedScenario(name = "scenario4") { } }
                val scenarios = listOf(s1, s2)

                every {
                    constructedWith<GherkinScenarioBuilder>(
                        EqMatcher(
                            listOf(
                                "packageFromConf1",
                                "packageFromConf2"
                            )
                        )
                    ).resourceToScenarios(any())
                } returns scenarios

                val res = KestGherkinTestEngine().discover(request, UniqueId.forEngine(KestGherkinTestEngineName))

                verify {
                    constructedWith<GherkinScenarioBuilder>(
                        EqMatcher(
                            listOf(
                                "packageFromConf1",
                                "packageFromConf2"
                            )
                        )
                    ).resourceToScenarios(listOf("file content 1", "file content 2"))
                }


                assertTrue(res.isRoot)
                assertEquals(2, res.children.size)
                assertTrue(res.children.all { it is FeatureTestDescriptor })
                assertEquals("s1", res.children.first().displayName)
                assertEquals("s2", res.children.last().displayName)
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s1]", res.children.first().uniqueId.toString())
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s2]", res.children.last().uniqueId.toString())

                assertEquals(2, res.children.first().children.size)
                assertTrue(res.children.first().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario1", res.children.first().children.first().displayName)
                assertEquals("scenario2", res.children.first().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario1]",
                    res.children.first().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario2]",
                    res.children.first().children.last().uniqueId.toString()
                )

                assertEquals(2, res.children.last().children.size)
                assertTrue(res.children.last().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario3", res.children.last().children.first().displayName)
                assertEquals("scenario4", res.children.last().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario3]",
                    res.children.last().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario4]",
                    res.children.last().children.last().uniqueId.toString()
                )


            }

        }
    }

    @Test
    fun `kest gherkin test engines discovers tests from KestGherkinDiscoverySelector`() {

        val selector = KestGherkinFilterDiscoverySelector(
            id = "[engine:KestGherkinTestEngine]/[feature:s1]",
            selectPackage("com.github.lemfi.kest.gherkin.junit5")
        )

        mockkConstructor(GherkinScenarioBuilder::class) {

            mockkStatic(
                GherkinScenarioBuilder::resourceToScenarios,
                KestGherkinDiscoverySelector::toFeaturesDiscoveryConfiguration
            ) {

                val request = object : EngineDiscoveryRequest {
                    override fun <T : DiscoverySelector> getSelectorsByType(cls: Class<T>): MutableList<T> =
                        if (cls == KestGherkinDiscoverySelector::class.java) {
                            @Suppress("UNCHECKED_CAST")
                            mutableListOf(selector) as MutableList<T>
                        } else mutableListOf()

                    override fun <T : DiscoveryFilter<*>?> getFiltersByType(p0: Class<T>?): MutableList<T> {
                        throw NotImplementedError("...")
                    }

                    override fun getConfigurationParameters(): ConfigurationParameters {
                        throw NotImplementedError("...")
                    }
                }

                val fdc = listOf(
                    FeaturesDiscoveryConfiguration(
                        features = listOf("f"),
                        stepsPackages = listOf("s"),
                        source = ClasspathResourceSource.from("...")
                    )
                )

                every {
                    KestGherkinDiscoverySelector::toFeaturesDiscoveryConfiguration.invoke(
                        any(),
                        any(),
                        any()
                    )
                } returns fdc

                val s1 =
                    scenario(name = "s1") { nestedScenario(name = "scenario1") { }; nestedScenario(name = "scenario2") { } }
                val s2 =
                    scenario(name = "s2") { nestedScenario(name = "scenario3") { }; nestedScenario(name = "scenario4") { } }
                val scenarios = listOf(s1, s2)

                every { constructedWith<GherkinScenarioBuilder>(EqMatcher(listOf("s"))).resourceToScenarios(listOf("f")) } returns scenarios

                val res = KestGherkinTestEngine().discover(request, UniqueId.forEngine(KestGherkinTestEngineName))

                verify { constructedWith<GherkinScenarioBuilder>(EqMatcher(listOf("s"))).resourceToScenarios(listOf("f")) }

                verify {
                    KestGherkinDiscoverySelector::toFeaturesDiscoveryConfiguration.invoke(
                        selector,
                        emptySet(),
                        any()
                    )
                }

                assertTrue(res.isRoot)
                assertEquals(2, res.children.size)
                assertTrue(res.children.all { it is FeatureTestDescriptor })
                assertEquals("s1", res.children.first().displayName)
                assertEquals("s2", res.children.last().displayName)
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s1]", res.children.first().uniqueId.toString())
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s2]", res.children.last().uniqueId.toString())

                assertEquals(2, res.children.first().children.size)
                assertTrue(res.children.first().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario1", res.children.first().children.first().displayName)
                assertEquals("scenario2", res.children.first().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario1]",
                    res.children.first().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario2]",
                    res.children.first().children.last().uniqueId.toString()
                )

                assertEquals(2, res.children.last().children.size)
                assertTrue(res.children.last().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario3", res.children.last().children.first().displayName)
                assertEquals("scenario4", res.children.last().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario3]",
                    res.children.last().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario4]",
                    res.children.last().children.last().uniqueId.toString()
                )

            }

        }
    }

    @Test
    fun `kest gherkin test engines discovers tests from ClassSelector and filters scenarios when filter is provided`() {

        mockkConstructor(GherkinScenarioBuilder::class) {

            mockkStatic(
                Collection<ClassSelector>::toFeaturesDiscoveryConfiguration,
                GherkinScenarioBuilder::resourceToScenarios
            ) {

                val request = object : EngineDiscoveryRequest {
                    override fun <T : DiscoverySelector> getSelectorsByType(cls: Class<T>): MutableList<T> =
                        if (cls == ClassSelector::class.java) {
                            @Suppress("UNCHECKED_CAST")
                            mutableListOf<ClassSelector>(DiscoverySelectors.selectClass(FeatureClass::class.java)) as MutableList<T>
                        } else mutableListOf()

                    override fun <T : DiscoveryFilter<*>?> getFiltersByType(p0: Class<T>?): MutableList<T> {
                        throw NotImplementedError("...")
                    }

                    override fun getConfigurationParameters(): ConfigurationParameters {
                        throw NotImplementedError("...")
                    }
                }

                val fdc = listOf(
                    FeaturesDiscoveryConfiguration(
                        features = listOf("f"),
                        stepsPackages = listOf("s"),
                        source = ClassSource.from(FeatureClass::class.java),
                        filter = listOf(
                            "[engine:KestGherkinTestEngine]/[feature:s2]",
                            "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario2]"
                        )
                    )
                )

                every { Collection<ClassSelector>::toFeaturesDiscoveryConfiguration.invoke(any()) } returns fdc

                val s1 =
                    scenario(name = "s1") { nestedScenario(name = "scenario1") { }; nestedScenario(name = "scenario2") { } }
                val s2 =
                    scenario(name = "s2") { nestedScenario(name = "scenario3") { }; nestedScenario(name = "scenario4") { } }
                val s3 =
                    scenario(name = "s3") { nestedScenario(name = "scenario5") { }; nestedScenario(name = "scenario6") { } }
                val scenarios = listOf(s1, s2, s3)

                every { anyConstructed<GherkinScenarioBuilder>().resourceToScenarios(any()) } returns scenarios

                val res = KestGherkinTestEngine().discover(request, UniqueId.forEngine(KestGherkinTestEngineName))

                assertTrue(res.isRoot)
                assertEquals(2, res.children.size)
                assertTrue(res.children.all { it is FeatureTestDescriptor })
                assertEquals("s1", res.children.first().displayName)
                assertEquals("s2", res.children.last().displayName)
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s1]", res.children.first().uniqueId.toString())
                assertEquals("[engine:KestGherkinTestEngine]/[feature:s2]", res.children.last().uniqueId.toString())

                assertEquals(2, res.children.last().children.size)
                assertTrue(res.children.last().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario3", res.children.last().children.first().displayName)
                assertEquals("scenario4", res.children.last().children.last().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario3]",
                    res.children.last().children.first().uniqueId.toString()
                )
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s2]/[scenario:scenario4]",
                    res.children.last().children.last().uniqueId.toString()
                )

                assertEquals(1, res.children.first().children.size)
                assertTrue(res.children.first().children.all { it is NestedStepTestDescriptor })
                assertEquals("scenario2", res.children.first().children.first().displayName)
                assertEquals(
                    "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario2]",
                    res.children.first().children.first().uniqueId.toString()
                )
            }

        }
    }

    @Test
    fun `kest gherkin test engine executes test plan`() {
        val engine = KestGherkinTestEngine()
        val child1 = mockk<FeatureTestDescriptor>(relaxUnitFun = true)
        val child2 = mockk<NestedStepTestDescriptor>(relaxUnitFun = true)
        val child3 = mockk<StepTestDescriptor>(relaxUnitFun = true)

        val testDescriptor =
            KestGherkinTestEngineDescriptor(UniqueId.forEngine(KestGherkinTestEngineName), KestGherkinTestEngineName)
                .apply {
                    addChild(child1)
                    addChild(child2)
                    addChild(child3)
                }

        val executionListener = mockk<EngineExecutionListener>(relaxUnitFun = true)

        every { child1.execute(any(), null) } returns KestGherkinEngineExecutionContext(executionListener)

        val request = ExecutionRequest(testDescriptor, executionListener, null)
        engine.execute(request)

        verify { child1.setParent(testDescriptor) }
        verify { child2.setParent(testDescriptor) }
        verify { child3.setParent(testDescriptor) }

        verify { executionListener.dynamicTestRegistered(request.rootTestDescriptor) }
        verify { executionListener.executionStarted(request.rootTestDescriptor) }
        verify { executionListener.executionFinished(request.rootTestDescriptor, TestExecutionResult.successful()) }
        verify { child1.execute(any(), null) }
        verify(exactly = 0) { child2.execute(any(), any()) }
        verify(exactly = 0) { child3.execute(any(), any()) }
    }

    @Test
    fun `a feature test descriptor discovers its children at init`() {
        val feature =
            scenario(name = "s1") { nestedScenario(name = "scenario1") { }; nestedScenario(name = "scenario2") { } }

        val descriptor = FeatureTestDescriptor(feature, ClassSource.from(FeatureClass::class.java))

        assertEquals(TestDescriptor.Type.CONTAINER, descriptor.type)

        assertEquals(2, descriptor.children.size)
        assertEquals("scenario1", descriptor.children.first().displayName)
        assertEquals("scenario2", descriptor.children.last().displayName)
        assertEquals(
            "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario1]",
            descriptor.children.first().uniqueId.toString()
        )
        assertEquals(
            "[engine:KestGherkinTestEngine]/[feature:s1]/[scenario:scenario2]",
            descriptor.children.last().uniqueId.toString()
        )
    }

    @Test
    fun `a feature test descriptor executes scenarios`() {

        mockkStatic(NestedStepTestDescriptor::execute) {

            val feature =
                scenario(name = "s1") { nestedScenario(name = "scenario1") { }; nestedScenario(name = "scenario2") { } }

            val descriptor = FeatureTestDescriptor(feature, ClassSource.from(FeatureClass::class.java))

            val executionListener = mockk<EngineExecutionListener>(relaxUnitFun = true)

            val context = KestGherkinEngineExecutionContext(executionListener)
            descriptor.execute(context, null)

            assertEquals(2, descriptor.children.size)
            assertTrue(descriptor.children.all { it is NestedStepTestDescriptor })

            verifyOrder {
                executionListener.dynamicTestRegistered(descriptor)
                executionListener.executionStarted(descriptor)
                (descriptor.children.first() as NestedStepTestDescriptor).execute(context, null)
                (descriptor.children.last() as NestedStepTestDescriptor).execute(context, null)
                executionListener.executionFinished(descriptor, TestExecutionResult.successful())
            }
        }
    }

    @Test
    fun `a nested step test descriptor discovers on prepare`() {
        val feature =
            scenario(name = "s1") {
                nestedScenario(name = "scenario1") {
                    step(name = "step1") {}; nestedScenario(name = "step2") {}
                }
            }

        val descriptor = NestedStepTestDescriptor(
            UniqueId.forEngine(KestGherkinTestEngineName).append("feature", "s1"),
            feature.steps.first() as NestedScenarioStep<*>,
            ClassSource.from(FeatureClass::class.java)
        )

        assertEquals(TestDescriptor.Type.CONTAINER, descriptor.type)

        val listener = mockk<EngineExecutionListener>()
        val context = KestGherkinEngineExecutionContext(listener)
        descriptor.prepare(context)

        assertEquals(2, descriptor.children.size)
        assertEquals("step1", descriptor.children.first().displayName)
        assertEquals("step2", descriptor.children.last().displayName)
        assertEquals(
            "[engine:KestGherkinTestEngine]/[feature:s1]/[nestedScenario:scenario1]/[step:step1]",
            descriptor.children.first().uniqueId.toString()
        )
        assertEquals(
            "[engine:KestGherkinTestEngine]/[feature:s1]/[nestedScenario:scenario1]/[nestedScenario:step2]",
            descriptor.children.last().uniqueId.toString()
        )
    }

    @Test
    fun `when build of nested scenario fails step is set to Failed status`() {
        val feature =
            scenario(name = "s1") {
                nestedScenario(name = "scenario1") {
                    throw NullPointerException("bam")
                }
            }

        val descriptor = NestedStepTestDescriptor(
            UniqueId.forEngine(KestGherkinTestEngineName).append("feature", "s1"),
            feature.steps.first() as NestedScenarioStep<*>,
            ClassSource.from(FeatureClass::class.java)
        )

        assertEquals(TestDescriptor.Type.CONTAINER, descriptor.type)

        val listener = mockk<EngineExecutionListener>()
        val context = KestGherkinEngineExecutionContext(listener)

        val exception = assertThrows<NullPointerException> { descriptor.prepare(context) }
        assertEquals("bam", exception.message)

        assertTrue(feature.steps.first().postExecution.isFailed())
    }

    @Test
    fun `nested step descriptor executes nested scenario`() {

        var step1ExecutionPlayed = false
        var step2ExecutionPlayed = false
        var nestedStepResultBuilt = false

        val feature =
            scenario(name = "s1") {
                nestedScenario<Boolean>(name = "scenario1") {

                    step(name = "step1") {
                        step1ExecutionPlayed = true
                    }; nestedScenario(name = "step2") { step { step2ExecutionPlayed = true } }

                    returns { nestedStepResultBuilt = true; true }
                }
            }

        val descriptor = NestedStepTestDescriptor(
            UniqueId.forEngine(KestGherkinTestEngineName).append("feature", "s1"),
            feature.steps.first() as NestedScenarioStep<*>,
            ClassSource.from(FeatureClass::class.java)
        )

        val listener = mockk<EngineExecutionListener>(relaxUnitFun = true)
        val context = KestGherkinEngineExecutionContext(listener)

        val res = descriptor.execute(context, null)

        assertEquals(context, res)
        assertTrue(step1ExecutionPlayed)
        assertTrue(step2ExecutionPlayed)
        assertTrue(nestedStepResultBuilt)

        verifyOrder {
            listener.dynamicTestRegistered(descriptor)
            listener.executionStarted(descriptor)
            listener.executionFinished(descriptor, TestExecutionResult.successful())
        }
    }

    @Test
    fun `step descriptor executes step`() {

        val feature =
            scenario(name = "s1") {
                step(name = "step1") { "HELLO" }
            }

        val descriptor = StepTestDescriptor(
            UniqueId.forEngine(KestGherkinTestEngineName).append("feature", "s1"),
            feature.steps.first(),
            ClassSource.from(FeatureClass::class.java)
        )
        assertEquals(TestDescriptor.Type.TEST, descriptor.type)

        val listener = mockk<EngineExecutionListener>(relaxUnitFun = true)
        val context = KestGherkinEngineExecutionContext(listener)

        val res = descriptor.execute(context, null)

        assertEquals(context, res)

        assertTrue(feature.steps.first().postExecution.isSuccess())
        assertEquals("HELLO", feature.steps.first().postExecution())

        verifyOrder {
            listener.dynamicTestRegistered(descriptor)
            listener.executionStarted(descriptor)
            listener.executionFinished(descriptor, TestExecutionResult.successful())
        }
    }

    @Test
    fun `step descriptor execution works as espected even when step fails on assertions`() {

        val feature =
            scenario(name = "s1") {
                step(name = "step1") { "HELLO" } assertThat { false.isTrue }
            }

        val descriptor = StepTestDescriptor(
            UniqueId.forEngine(KestGherkinTestEngineName).append("feature", "s1"),
            feature.steps.first(),
            ClassSource.from(FeatureClass::class.java)
        )

        val listener = mockk<EngineExecutionListener>(relaxUnitFun = true)
        val context = KestGherkinEngineExecutionContext(listener)

        val res = descriptor.execute(context, null)

        assertEquals(context, res)

        assertTrue(feature.steps.first().postExecution.isFailed())

        val testExecutionResult = slot<TestExecutionResult>()

        verifyOrder {
            listener.dynamicTestRegistered(descriptor)
            listener.executionStarted(descriptor)
            listener.executionFinished(descriptor, capture(testExecutionResult))
        }

        assertEquals(TestExecutionResult.Status.FAILED, testExecutionResult.captured.status)
        assertInstanceOf(AssertionFailedError::class.java, testExecutionResult.captured.throwable.get())
        assertEquals(
            """

Scenario: s1
    Step: step1

Expected true, was false
""", testExecutionResult.captured.throwable.get().message
        )
    }

    @Test
    fun `step descriptor execution works as espected even when step fails on invoking result from previous step`() {

        val feature =
            scenario(name = "s1") {
                val step1 = step(name = "step1") { } assertThat { false.isTrue }
                step(name = "step2") { step1(); "HELLO" } assertThat { true.isTrue }
            }

        val descriptor = StepTestDescriptor(
            UniqueId.forEngine(KestGherkinTestEngineName).append("feature", "s1"),
            feature.steps.last(),
            ClassSource.from(FeatureClass::class.java)
        )

        val listener = mockk<EngineExecutionListener>(relaxUnitFun = true)
        val context = KestGherkinEngineExecutionContext(listener)

        val res = descriptor.execute(context, null)

        assertEquals(context, res)

        assertFalse(feature.steps.first().postExecution.isFailed())
        assertFalse(feature.steps.first().postExecution.isSuccess())

        val skipReason = slot<String>()

        verifyOrder {
            listener.dynamicTestRegistered(descriptor)
            listener.executionStarted(descriptor)
            listener.executionSkipped(descriptor, capture(skipReason))
        }

        assertEquals(
            """
Step "step1" was not played yet! 
You may use its result only in another step body
""", skipReason.captured
        )
    }

}


private class FeatureClass