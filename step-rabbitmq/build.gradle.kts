val coroutineVersion: String by project.rootProject.extra
val rabbitmqVersion: String by project.rootProject.extra

dependencies {

    implementation(project(":core"))

    implementation("com.rabbitmq:amqp-client:$rabbitmqVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
}


signing {
    sign(publishing.publications["mavenJava"])
}