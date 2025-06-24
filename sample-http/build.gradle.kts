
dependencies {

    implementation(project(":core"))
    implementation(project(":step-http"))
    implementation(project(":junit5"))

    implementation(libs.slf4j.simple)

    implementation(libs.kotlin.coroutines.core)

}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}