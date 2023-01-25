val rabbitmqVersion: String by project
val okhttpVersion: String by project

dependencies {

    implementation(project(":core"))
    implementation(project(":json"))

    implementation("com.rabbitmq:amqp-client:$rabbitmqVersion")

    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
}


signing {
    sign(publishing.publications["mavenJava"])
}