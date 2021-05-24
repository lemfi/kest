//pluginManagement {
//    repositories {
//        gradlePluginPortal()
//        jcenter()
//        mavenCentral()
//        maven("https://dl.bintray.com/kotlin/kotlin-eap")
//        maven("https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
//    }
//}

rootProject.name = "kest"
include("core", "json", "step-http", "junit5", "step-rabbitmq", "step-mongodb", "step-cadence", "sample")

