val okhttpVersion: String by project.rootProject.extra

dependencies {

    implementation(project(":core"))
    api(project(":json"))

    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
}


signing {
    sign(publishing.publications["mavenJava"])
}