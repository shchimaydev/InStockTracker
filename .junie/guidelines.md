# InStockTracker – Project Overview

InStockTracker is a Kotlin Multiplatform (KMP) project for tracking inventory across platforms. It consists of:

- Android app: Jetpack Compose-based mobile app
- iOS app: Native iOS app leveraging KMP shared code
- Server: Ktor-based backend for data management and scheduling
- Shared module: Common code (models, networking, logic) used by all targets

## Architecture at a Glance
- composeApp: Compose Multiplatform UI and Android-specific implementations
- iosApp: Xcode project and iOS entry points
- server: Ktor server (Netty), REST endpoints, logging (Logback)
- shared: Common code (commonMain) + platform-specific implementations (Android, iOS, JVM)
- designs: UI mockups (Main-Screen.jpg, Add-Edit.jpg)

## Key Technologies
- Kotlin Multiplatform 2.2.0, Compose Multiplatform 1.8.2
- Ktor 3.2.0 (server and client)
- Google Cloud (Firestore, Scheduler)
- AndroidX stack for Android, Kotlin Test/JUnit/Espresso for testing

## Build & Run
- Build: ./gradlew build
- Android app: Run composeApp in Android Studio/IntelliJ on device/emulator
- iOS app: Open iosApp in Xcode and run on simulator/device
- Server: ./gradlew :server:run

## Conventions
- Follow Kotlin coding conventions and document public APIs
- Adhere strictly to designs in /designs when implementing UI
- Add tests for new functionality

For more details, see DOCUMENTATION.md at the project root.