val junitVersion: String by project.rootProject.extra

dependencies {

    api(project(":core"))

    api("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

signing {
    sign(publishing.publications["mavenJava"])
}


tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}