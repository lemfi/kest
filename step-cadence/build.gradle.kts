dependencies {

    implementation(project(":core"))

    implementation(libs.jackson.kotlin) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }

    implementation(libs.cadence.client)
}


signing {
    sign(publishing.publications["mavenJava"])
}