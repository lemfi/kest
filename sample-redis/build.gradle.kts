
dependencies {

    implementation(project(":core"))
    implementation(project(":step-redis"))
    implementation(project(":junit5"))

    implementation(libs.slf4j.simple)

}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}