val opentest4jVersion: String by project.rootProject.extra
val hopliteVersion: String by project.rootProject.extra
val slf4jVersion: String by project.rootProject.extra

dependencies {

    api("org.opentest4j:opentest4j:$opentest4jVersion")
    api("org.slf4j:slf4j-api:$slf4jVersion")

    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
    implementation("com.sksamuel.hoplite:hoplite-yaml:$hopliteVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
 }

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}

signing {
    sign(publishing.publications["mavenJava"])
}