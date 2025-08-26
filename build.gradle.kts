import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.deps.updates.verification)
    }
}

plugins {
    alias(libs.plugins.dokka.core)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.maven.publish)
    signing
    jacoco
    kotlin("jvm") version libs.versions.kotlin.asProvider().get()
}

allprojects {

    apply(plugin = rootProject.libs.plugins.dokka.core.get().pluginId)
    apply(plugin = rootProject.libs.plugins.dokka.javadoc.get().pluginId)

    repositories {
        mavenLocal()
        mavenCentral()
    }

    tasks.withType(Sign::class.java) {
        onlyIf { isRelease }
    }
}

val isRelease = !(project.version as String).endsWith("SNAPSHOT")
val Project.noSample: Boolean get() = !name.startsWith("sample")

allprojects {

    apply(plugin = rootProject.libs.plugins.deps.updates.verification.get().pluginId)

    tasks.withType<DependencyUpdatesTask> {
        rejectVersionIf {
            candidate.version.lowercase().contains("snapshot")
                    || candidate.version.lowercase().contains("rc")
                    || candidate.version.lowercase().contains("beta")
                    || candidate.version.lowercase().contains("alpha")
                    || candidate.version.lowercase().contains("-m1")
                    || candidate.version.lowercase().contains("-m2")
        }
    }
}

subprojects {

    group = parent!!.group
    version = parent!!.version

    apply(plugin = "kotlin")
    apply(plugin = "java")
    apply(plugin = "signing")
    apply(plugin = "jacoco")
    apply(plugin = "com.vanniktech.maven.publish")

    jacoco {
        toolVersion = "0.8.7"
    }

    kotlin {
        jvmToolchain(17)
    }

    dokka {

        dokkaSourceSets.main {
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl("https://github.com/lemfi/kest")
                remoteLineSuffix.set("#L")
            }
        }
    }

    dependencies {
        "implementation"(rootProject.libs.kotlin.stdlib)
        "implementation"(rootProject.libs.kotlin.reflect)

        "implementation"(rootProject.libs.opentest4j)

    }

    tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(project.the<SourceSetContainer>()["main"].allSource)
    }

    tasks.withType<Javadoc> {
        source = project.the<SourceSetContainer>()["main"].allSource

        options.encoding("UTF-8")
    }

    tasks.register<Jar>("javadocJar") {
        dependsOn("dokkaGenerate")
        archiveClassifier.set("javadoc")
        from(layout.buildDirectory.get().asFile.path + "/dokka/javadoc")

        onlyIf { isRelease }
    }

    mavenPublishing {
        publishToMavenCentral()

        signAllPublications()
        coordinates(parent!!.group as String?, project.name, parent!!.version as String?)

        pom {
            name.set("Kest")
            description.set("Backends testing with Kotlin")
            url.set("https://github.com/lemfi/kest")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("lemfi")
                    name.set("Fiona Le Montreer")
                    email.set("fiona.le.montreer@gmail.com")
                }
            }
            scm {
                url.set("https://github.com/lemfi/kest")
            }
        }
    }

    tasks.withType<PublishToMavenRepository> {
        enabled = noSample
    }

    tasks.register<DefaultTask>("install") {
        group = "Publishing"
        description = "Publishes Maven publication '${project.name}' to the local Maven repository"

        dependsOn("publishToMavenLocal")
    }

    tasks.withType<Test> {
        description = "Runs the unit and integration tests"
        useJUnitPlatform()
        finalizedBy(tasks.named("jacocoTestReport")) // report is always generated after tests run
    }

}

jacoco {
    toolVersion = "0.8.7"
}

tasks.create<JacocoReport>("jacoco") {

    dependsOn(
        subprojects
        .filterNot { it.name.startsWith("sample") }
        .map { it.tasks.withType<Test>() }
    )

    executionData(
        subprojects
        .filterNot { it.name.startsWith("sample") }
        .map { it.fileTree("build/jacoco/test.exec") })
    sourceSets(
        *(subprojects
        .filterNot { it.name.startsWith("sample") }
        .map { it.the<SourceSetContainer>()["main"] as SourceSet }).toTypedArray()
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }

    doLast {
        File("build/reports/jacoco/jacoco/jacoco.csv")
            .readText(Charsets.UTF_8)
            .lines()
            .drop(1)
            .filterNot { it.isEmpty() }
            .map { line ->
                line
                    .split(",")
                    .drop(3)
                    .take(2)
                    .let {
                        (it.first().toInt() + it.last().toInt()) to it.last().toInt()
                    }
            }
            .reduce { (accAll, accCovered), (all, covered) -> (accAll + all) to (accCovered + covered) }
            .apply { println("Coverage: ${second * 100 / first}%") }
    }
}

