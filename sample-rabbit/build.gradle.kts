val rabbitmqVersion: String by project.rootProject.extra
val junitVersion: String by project.rootProject.extra
val coroutineVersion: String by project.rootProject.extra

dependencies {

    implementation("com.rabbitmq:amqp-client:$rabbitmqVersion")

    implementation(project(":core"))
    implementation(project(":step-rabbitmq"))
    implementation(project(":junit5"))

    implementation("org.slf4j:slf4j-simple:1.7.28")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")

}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Test> {
    description = "Runs the unit and integration tests"
    useJUnitPlatform()
}