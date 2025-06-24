
dependencies {

    implementation(project(":core"))
    implementation(project(":step-redis"))
    implementation(project(":junit5"))

    implementation(libs.slf4j.simple)

}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}