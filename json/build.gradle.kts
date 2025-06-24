
dependencies {

    implementation(project(":core"))

    api(libs.jackson.kotlin) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

}

tasks.withType<Test> {
    description = "Runs the unit tests"
    useJUnitPlatform()
}