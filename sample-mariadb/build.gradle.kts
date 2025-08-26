val slf4jVersion: String by project

dependencies {

    implementation(project(":core"))
    implementation(project(":step-mariadb"))
    implementation(project(":junit5"))

    implementation(libs.slf4j.simple)

    implementation(libs.testcontainers)
    implementation(libs.testcontainers.mariadb)

}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}