
dependencies {

    implementation(project(":core"))

    implementation(libs.cucumber.gherkin)

    implementation(libs.kotlin.stdlib.common)
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(libs.reflections)
    implementation(libs.slf4j.simple)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}