val junitVersion: String by project.rootProject.extra
val jacksonVersion: String by project.rootProject.extra

dependencies {

    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    implementation(project(":core"))

    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

}

tasks.withType<Test> {
    description = "Runs the unit tests"
    useJUnitPlatform()
}

signing {
    sign(publishing.publications["mavenJava"])
}