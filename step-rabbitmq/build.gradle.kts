val coroutineVersion: String by project
val rabbitmqVersion: String by project

dependencies {

    implementation(project(":core"))

    implementation("com.rabbitmq:amqp-client:$rabbitmqVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
}


signing {
    sign(publishing.publications["mavenJava"])
}