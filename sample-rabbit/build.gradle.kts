
dependencies {

    implementation(libs.rabbitmq.client)

    implementation(project(":core"))
    implementation(project(":step-rabbitmq"))
    implementation(project(":junit5"))

    implementation(libs.slf4j.simple)

    implementation(libs.kotlin.coroutines.core)
    implementation(libs.testcontainers)
    implementation(libs.testcontainers.rabbitmq)

}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}