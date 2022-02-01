val cadenceVersion: String by project
val junitVersion: String by project
val coroutineVersion: String by project
val slf4jVersion: String by project

dependencies {

    implementation(project(":core"))
    implementation(project(":step-cadence"))
    implementation(project(":junit5"))

    implementation("org.slf4j:slf4j-simple:$slf4jVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")

    implementation("com.uber.cadence:cadence-client:$cadenceVersion")

}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}