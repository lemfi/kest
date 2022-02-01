val junitVersion: String by project
val jacksonVersion: String by project

dependencies {

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