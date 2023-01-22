val okhttpVersion: String by project
val kotlinVersion: String by project
val cucumberVersion: String by project
val junitVersion: String by project
val slf4jVersion: String by project
val reflectionsVersion: String by project

dependencies {

    implementation(project(":core"))

    implementation("io.cucumber:gherkin:$cucumberVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}

signing {
    sign(publishing.publications["mavenJava"])
}