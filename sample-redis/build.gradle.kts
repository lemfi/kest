val junitVersion: String by project.rootProject.extra
val slf4jVersion: String by project

dependencies {

    implementation(project(":core"))
    implementation(project(":step-redis"))
    implementation(project(":junit5"))

    implementation("org.slf4j:slf4j-simple:$slf4jVersion")

}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}