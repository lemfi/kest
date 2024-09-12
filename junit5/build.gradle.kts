dependencies {

    api(project(":core"))

    implementation(libs.junit.launcher)
    implementation(libs.reflections)

    api(libs.junit.jupiter.api)
    runtimeOnly(libs.junit.jupiter.engine)
}

signing {
    sign(publishing.publications["mavenJava"])
}


tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}