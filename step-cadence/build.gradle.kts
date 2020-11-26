val kotlinVersion: String by project.rootProject.extra
val jacksonVersion: String by project.rootProject.extra
val cadenceVersion: String by project.rootProject.extra

dependencies {

    implementation(project(":core"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }

    implementation("com.uber.cadence:cadence-client:$cadenceVersion")
}


signing {
    sign(publishing.publications["mavenJava"])
}