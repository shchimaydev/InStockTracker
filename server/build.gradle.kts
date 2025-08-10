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

// build.gradle

tasks.register<Exec>("deploy") {
    group = "Deployment"
    description = "Deploys the server application to Google App Engine."

    // This task should run after the jar has been built.
    dependsOn("buildFatJar")

    // The command to execute.
    // Make sure 'gcloud' is available in your system's PATH.
    commandLine(
        "gcloud",
        "app",
        "deploy",
        "build/libs/server-all.jar", // Path to the JAR within the server module
        "--project",
        "instocktracker-464721"
    )
}


dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.statusPages)
    implementation(libs.ktor.sessions)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.jdk)
    implementation(platform(libs.google.cloud.bom))
    implementation(libs.google.cloud.firestore)
    implementation(libs.google.cloud.scheduler)
    implementation(libs.google.api.client)
    implementation(libs.google.genai)
    implementation(libs.selenium)



    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.mockk)
}
