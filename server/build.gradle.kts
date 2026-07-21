plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlinSerialization)
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

tasks.register<Exec>("gcloudDeploy") {
    group = "Deployment"
    description = "Deploys the server application to Google App Engine."

    // This task should run after the jar has been built.
    dependsOn("buildFatJar")

    // The command to execute.
    // Make sure 'gcloud' is available in your system's PATH.
    commandLine(
        "/Users/illya/y/google-cloud-sdk/bin/gcloud",
        "app",
        "deploy",
        "build/libs/server-all.jar", // Path to the JAR within the server module
        "--project",
        "instocktracker-464721",
        "--quiet"
    )
}


dependencies {
    implementation(projects.shared)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.statusPages)
    implementation(libs.ktor.sessions)
    implementation(libs.ktor.auth)
    implementation(libs.ktor.auth.jwt)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.encoding)
    implementation(libs.jsoup)


    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.jdk)
    implementation(libs.kotlinx.datetime)


    implementation(platform(libs.google.cloud.bom))
    implementation(libs.google.cloud.firestore)
    implementation(libs.google.cloud.scheduler)
    implementation(libs.google.api.client)
    implementation(libs.google.genai)
    implementation(libs.google.firebase.admin)

    implementation(libs.selenium)
    implementation(libs.logback)
    implementation(libs.dotenv.kotlin)
    implementation("io.ktor:ktor-client-logging:3.2.3")
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.mockk)
    testImplementation("io.ktor:ktor-client-mock:3.2.3")
}
