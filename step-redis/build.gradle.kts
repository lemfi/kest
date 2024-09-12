
dependencies {

    implementation(project(":core"))

    implementation(libs.redis.client)
}


signing {
    sign(publishing.publications["mavenJava"])
}