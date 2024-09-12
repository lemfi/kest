
dependencies {

    implementation(project(":core"))
    implementation(project(":json"))

    implementation(libs.mongo.driver.sync)
}


signing {
    sign(publishing.publications["mavenJava"])
}