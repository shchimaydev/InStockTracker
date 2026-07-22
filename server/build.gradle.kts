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

tasks.register<Exec>("cloudRunDeploy") {
    group = "Deployment"
    description = "Builds the fat jar and deploys it to Cloud Run."

    // This task should run after the jar has been built.
    dependsOn("buildFatJar")

    // The command to execute.
    // Make sure 'gcloud' is available in your system's PATH.
    //
    // --source . uses the Dockerfile in this directory (built via Cloud Build,
    // no local Docker needed) to containerize the already-built fat jar.
    // Secrets are pulled from Secret Manager at deploy time rather than
    // baked into the image.
    commandLine(
        "gcloud",
        "run",
        "deploy",
        "instocktracker-server",
        "--source",
        ".",
        "--region",
        "europe-west3",
        "--project",
        "instocktracker-464721",
        "--allow-unauthenticated",
        "--memory",
        "512Mi",
        "--cpu",
        "1",
        "--concurrency",
        "10",
        "--min-instances",
        "0",
        "--max-instances",
        "3",
        "--set-secrets",
        "GEMINI_API_KEY=GEMINI_API_KEY:latest,BROWSERLESSIO_TOKEN=BROWSERLESSIO_TOKEN:latest,SCHEDULER_CALLER_SA=SCHEDULER_CALLER_SA:latest",
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
