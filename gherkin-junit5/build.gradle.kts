
dependencies {

    api(project(":gherkin-core"))

    implementation(project(":core"))

    implementation(libs.junit.jupiter.engine)

    implementation(libs.kotlin.stdlib.common)
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(libs.reflections)
    implementation(libs.slf4j.simple)

    testImplementation(project(":step-http"))
    testImplementation(libs.mockk.core)
}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}