import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group  = "com.github.lemfi.kest"
version = "0.0.10"

val kotlinVersion : String by extra { "1.4.10" }
val coroutineVersion : String by extra { "1.4.1" }
val opentest4jVersion : String by extra { "1.2.0" }
val hopliteVersion : String by extra { "1.3.8" }
val slf4jVersion : String by extra { "1.7.28" }
val junitVersion : String by extra { "5.5.0" }
val jacksonVersion : String by extra { "2.11.3" }
val okhttpVersion : String by extra { "4.9.0" }
val cadenceVersion : String by extra { "2.7.8" }
val mongoVersion : String by extra { "4.1.1" }
val rabbitmqVersion : String by extra { "5.7.3" }

buildscript {
    val kotlinVersion : String by extra { "1.4.10" }
    val dokkaVersion : String by extra { "1.4.10.2" }

    repositories {
        jcenter()
        mavenCentral()
        maven(url="https://dl.bintray.com/kotlin/dokka")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:${dokkaVersion}")
    }
}

plugins {
    signing
    id("org.jetbrains.dokka") version "1.4.10.2"
}

allprojects {

    apply(plugin = "org.jetbrains.dokka")

    repositories {
        mavenLocal()
        jcenter()
        maven(url="https://dl.bintray.com/kotlin/dokka")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
        mavenCentral()
    }
    dependencies {
        dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.4.10.2")
    }
}

subprojects {

    group = parent!!.group
    version = parent!!.version



    apply(plugin = "kotlin")
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    compileKotlin.kotlinOptions.jvmTarget = "1.8"

    val compileTestKotlin: KotlinCompile by tasks
    compileTestKotlin.kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    compileTestKotlin.kotlinOptions.jvmTarget = "1.8"

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

        "implementation"("org.opentest4j:opentest4j:$opentest4jVersion")

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
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])

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
                val releaseUrl =  uri(project.properties["PUBLISH_RELEASE"] as String)
                val snapshotsUrl =  uri(project.properties["PUBLISH_SNAPSHOT"] as String)
                url = if ((project.version as String).endsWith("SNAPSHOT")) snapshotsUrl else releaseUrl
                credentials {
                    username = project.properties["PUBLISH_USERNAME"] as String
                    password = project.properties["PUBLISH_PASSWORD"] as String
                }
            }
        }
    }

}

tasks.register<DefaultTask>("install") {
    subprojects.forEach { project -> dependsOn("${project.name}:publishToMavenLocal") }
    group = "Publishing"
    description = "Publishes Maven publication '${project.name}' to the local Maven repository"
}
