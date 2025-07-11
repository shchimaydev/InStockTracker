plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.shadow)
    application
}

group = "com.ist.instocktracker"
version = "1.0.0"
application {
    mainClass.set("com.ist.instocktracker.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}


dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(platform(libs.google.cloud.bom))
    implementation(libs.google.cloud.firestore)
    implementation(libs.google.cloud.scheduler)
//    implementation(libs.kotlinx.coroutines.guava)

    // By adding guava here, you instruct Gradle to enforce the version
    // provided by the google-cloud-bom, resolving the conflict.
//    implementation(libs.guava)



    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}