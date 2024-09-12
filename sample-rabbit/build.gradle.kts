
dependencies {

    implementation(libs.rabbitmq.client)

    implementation(project(":core"))
    implementation(project(":step-rabbitmq"))
    implementation(project(":junit5"))

    implementation(libs.slf4j.simple)

    implementation(libs.kotlin.coroutines.core)

}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}