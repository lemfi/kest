import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.github.lemfi.kest"
version = "0.6.9"

buildscript {
    val kotlinVersion: String by project
    val dokkaVersion: String by project

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
    }
}

plugins {
    signing
    jacoco
    id("org.jetbrains.dokka") version "1.6.10"
}

allprojects {

    apply(plugin = "org.jetbrains.dokka")

    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.0")
    }

    tasks.withType(Sign::class.java) {
        onlyIf { isRelease }
    }
}

val isRelease = !(project.version as String).endsWith("SNAPSHOT")

subprojects {

    val kotlinVersion: String by project

    group = parent!!.group
    version = parent!!.version

    apply(plugin = "kotlin")
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "jacoco")

    jacoco {
        toolVersion = "0.8.7"
    }

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
    compileKotlin.kotlinOptions.jvmTarget = "11"

    val compileTestKotlin: KotlinCompile by tasks
    compileTestKotlin.kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
    compileTestKotlin.kotlinOptions.jvmTarget = "11"

    tasks.withType<DokkaTask> {
        onlyIf { isRelease }
    }

    tasks.withType<DokkaTask>().configureEach {
        dokkaSourceSets {
            named("main") {
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(java.net.URL("https://github.com/lemfi/kest"))
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
        "implementation"("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

        "implementation"("org.opentest4j:opentest4j:1.2.0")

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
        dependsOn("dokkaJavadoc")
        archiveClassifier.set("javadoc")
        from(buildDir.path + "/dokka/javadoc")

        onlyIf { isRelease }
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                artifact(tasks["sourcesJar"])
                if (isRelease) {
                    artifact(tasks["javadocJar"])
                }

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
        }
        repositories {
            maven {
                val releaseUrl = uri(project.properties.getOrDefault("PUBLISH_RELEASE", "NONE") as String)
                val snapshotsUrl = uri(project.properties.getOrDefault("PUBLISH_SNAPSHOT", "NONE") as String)
                url = if ((project.version as String).endsWith("SNAPSHOT")) snapshotsUrl else releaseUrl
                credentials {
                    username = project.properties.getOrDefault("PUBLISH_USERNAME", "NONE") as String
                    password = project.properties.getOrDefault("PUBLISH_PASSWORD", "NONE") as String
                }
            }
        }
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

    dependsOn(subprojects
        .filterNot { it.name.startsWith("sample") }
        .map { it.tasks.withType<Test>() }
    )

    executionData(subprojects
        .filterNot { it.name.startsWith("sample") }
        .map { it.fileTree("build/jacoco/test.exec") })
    sourceSets(*(subprojects
        .filterNot { it.name.startsWith("sample") }
        .map { it.the<SourceSetContainer>()["main"] as SourceSet }).toTypedArray()
    )

    reports {
        xml.isEnabled = true
        html.isEnabled = true
        csv.isEnabled = true
    }

    doLast {
        File("build/reports/jacoco/jacoco/jacoco.csv")
            .readText(java.nio.charset.Charset.defaultCharset())
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

