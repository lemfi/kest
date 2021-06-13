val junitVersion: String by project.rootProject.extra
val junitLauncherVersion: String by project.rootProject.extra

dependencies {

    api(project(":core"))

    implementation("org.junit.platform:junit-platform-launcher:$junitLauncherVersion")

    api("org.junit.jupiter:junit-jupiter-api:$junitVersion")
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