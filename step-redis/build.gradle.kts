val jedisVersion: String by project.rootProject.extra

dependencies {

    implementation(project(":core"))

    implementation("redis.clients:jedis:$jedisVersion")
}


signing {
    sign(publishing.publications["mavenJava"])
}