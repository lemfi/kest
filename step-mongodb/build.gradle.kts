val mongoVersion: String by project.rootProject.extra

dependencies {

    implementation(project(":core"))
    implementation(project(":json"))

    implementation("org.mongodb:mongodb-driver-sync:$mongoVersion")
}


signing {
    sign(publishing.publications["mavenJava"])
}