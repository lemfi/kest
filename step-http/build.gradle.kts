val okhttpVersion: String by project.rootProject.extra

dependencies {

    implementation(project(":core"))
    api(project(":json"))

    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
}


signing {
    sign(publishing.publications["mavenJava"])
}