package com.github.lemfi.kest.gherkin.junit5.discovery

import com.github.lemfi.kest.gherkin.junit5.KestGherkin
import com.github.lemfi.kest.gherkin.junit5.gherkinProperty
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.ClasspathRootSelector
import org.junit.platform.engine.discovery.DirectorySelector
import org.junit.platform.engine.discovery.FileSelector
import org.junit.platform.engine.discovery.PackageSelector
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.DirectorySource
import org.junit.platform.engine.support.descriptor.FileSource
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import java.io.File
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString


data class FeaturesDiscoveryConfiguration(
    val features: List<String>,
    val stepsPackages: Collection<String> = gherkinProperty { stepDefinitions },
    val filter: List<String> = emptyList(),
    val source: TestSource,
)

@JvmName("classpathRootSelectorToClasses")
internal fun Collection<ClasspathRootSelector>.toClasses(): List<Class<*>> = flatMap { it.toFeatureProviderClasses() }

@JvmName("classSelectorToClasses")
internal fun Collection<ClassSelector>.toClasses(): List<Class<*>> = flatMap { it.toFeatureProviderClasses() }


@JvmName("packageSelectorToClasses")
internal fun Collection<PackageSelector>.toClasses(): List<Class<*>> = flatMap { it.toFeatureProviderClasses() }

internal fun DiscoverySelector.toFeatureProviderClasses() =
    when (this) {

        is ClassSelector ->
            listOfNotNull(
                if (javaClass.declaredAnnotations.any { it is KestGherkin }) javaClass
                else null
            )

        is ClasspathRootSelector -> Reflections(
            ConfigurationBuilder()
                .addUrls(classpathRoot.toURL())
                .setScanners(Scanners.TypesAnnotated)
        ).getTypesAnnotatedWith(KestGherkin::class.java)

        is PackageSelector -> Reflections(
            ConfigurationBuilder()
                .forPackage(packageName)
                .filterInputsBy(FilterBuilder().includePackage(packageName))
                .setScanners(Scanners.TypesAnnotated)
        ).getTypesAnnotatedWith(KestGherkin::class.java)

        else -> emptyList()
    }

internal fun Class<*>.toFeaturesDiscoveryConfiguration(): List<FeaturesDiscoveryConfiguration> =
    declaredAnnotations
        .firstOrNull { it is KestGherkin }
        ?.toFeaturesDiscoveryConfiguration(this)
        ?: emptyList()

 fun Annotation.toFeaturesDiscoveryConfiguration(source: Class<*>): List<FeaturesDiscoveryConfiguration> =
    when (this) {

        is KestGherkin -> listOf(
            FeaturesDiscoveryConfiguration(
                features = resourceSources(path),
                stepsPackages = stepDefinitionsPackage.toList().ifEmpty { gherkinProperty { stepDefinitions } },
                source = ClassSource.from(source),
            )
        )

        else -> throw IllegalArgumentException("$this is not a recognized source provider")
    }

internal fun resourceSources(sourcesPath: String) =
    object {}.javaClass.getResource(sourcesPath)?.let { resource ->

        val uri: URI = resource.toURI()
        val walker: Triple<Path, (String) -> String, () -> Unit> = if (uri.scheme.equals("jar")) {
            FileSystems
                .newFileSystem(uri, emptyMap<String, Any>())
                .let { fileSystem ->
                    Triple(
                        fileSystem.getPath(sourcesPath),
                        { object {}.javaClass.getResourceAsStream(it)?.readAllBytes()?.toString(Charsets.UTF_8) ?: "" },
                        { fileSystem.close() })
                }
        } else {
            Triple(
                Paths.get(uri),
                { File(it).readText(Charsets.UTF_8) },
                {}
            )
        }

        walker.let { (path, resolver, quit) ->

            Files
                .walk(path)
                .filter { !it.isDirectory() && it.extension == "feature" }
                .toList()
                .map { resolver(it.pathString) }
                .apply { quit() }
        }

    } ?: emptyList()

@JvmName("classSelectorToSourceDefinition")
internal fun Collection<ClassSelector>.toFeaturesDiscoveryConfiguration() =
    toClasses().flatMap { it.toFeaturesDiscoveryConfiguration() }

@JvmName("classpathRootSelectorToSourceDefinition")
internal fun Collection<ClasspathRootSelector>.toFeaturesDiscoveryConfiguration() =
    toClasses().flatMap { it.toFeaturesDiscoveryConfiguration() }

@JvmName("packageSelectorToSourceDefinition")
internal fun Collection<PackageSelector>.toFeaturesDiscoveryConfiguration() =
    toClasses().flatMap { it.toFeaturesDiscoveryConfiguration() }

@JvmName("fileSelectorToSourceDefinition")
internal fun Collection<FileSelector>.toFeaturesDiscoveryConfiguration() =
    map {
        FeaturesDiscoveryConfiguration(
            features = listOf(it.file.readText(Charset.defaultCharset())),
            stepsPackages = gherkinProperty { stepDefinitions },
            source = FileSource.from(it.file),
        )
    }

@JvmName("directorySelectorToSourceDefinition")
internal fun Collection<DirectorySelector>.toFeaturesDiscoveryConfiguration() =
    map { directorySelector ->
        FeaturesDiscoveryConfiguration(
            features = directorySelector.directory
                .walkTopDown()
                .toList()
                .mapNotNull { if (it.isDirectory) null else it.readText(Charset.defaultCharset()) },
            stepsPackages = gherkinProperty { stepDefinitions },
            source = DirectorySource.from(directorySelector.directory),
        )
    }