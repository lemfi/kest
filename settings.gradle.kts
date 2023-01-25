rootProject.name = "kest"
include(
    "core",
    "json",
    "step-http",
    "junit5",
    "gherkin-core",
    "gherkin-junit5",
    "step-rabbitmq",
    "step-mongodb",
    "step-cadence",
    "step-redis",
    "sample-http",
    "sample-cadence",
    "sample-rabbit",
    "sample-redis",
)

pluginManagement {
    val dokkaVersion: String by settings

    plugins {
        id("org.jetbrains.dokka") version dokkaVersion
    }
}