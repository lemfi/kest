val jedisVersion: String by project

dependencies {

    implementation(project(":core"))

    implementation("redis.clients:jedis:$jedisVersion")
}


signing {
    sign(publishing.publications["mavenJava"])
}