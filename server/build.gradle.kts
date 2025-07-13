plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.shadow)
    application
}

kotlin {
    jvmToolchain(21)
}

group = "com.ist.instocktracker"
version = "1.0.0"
application {
    mainClass.set("com.ist.instocktracker.EngineKt")

    val isDevelopment: Boolean = project.ext.has("development")
    println("development: $isDevelopment")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}



dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.statusPages)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.google.cloud.bom))
    implementation(libs.google.cloud.firestore)
    implementation(libs.google.cloud.scheduler)


    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
