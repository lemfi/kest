
dependencies {

    implementation(project(":core"))
    implementation(project(":step-cadence"))
    implementation(project(":junit5"))

    implementation(libs.slf4j.simple)

    implementation(libs.kotlin.coroutines.core)

    implementation(libs.cadence.client)

}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}