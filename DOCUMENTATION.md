# InStockTracker - Project Documentation

## Project Overview

InStockTracker is a Kotlin Multiplatform project designed to track inventory across multiple platforms. The application
consists of:

- **Android App**: A mobile application built with Jetpack Compose
- **iOS App**: A native iOS application with Kotlin Multiplatform integration
- **Server**: A Ktor-based backend server for data management
- **Shared Module**: Common code shared between all platforms

The project is currently in its initial development phase, with basic structure and templates in place.

## Architecture

### Project Structure

- `/composeApp`: Contains the Compose Multiplatform application code
    - `commonMain`: Code shared across all Compose platforms
    - Platform-specific folders for platform-specific implementations

- `/iosApp`: Contains the iOS application entry point and SwiftUI code

- `/server`: Contains the Ktor server application
    - Currently implements a simple REST API

- `/shared`: Contains code shared between all targets
    - `commonMain`: Platform-independent code
    - Platform-specific implementations for Android, iOS, and JVM

- `/designs`: Contains UI design mockups and specifications
    - Currently includes Main-Screen.jpg and Add-Edit.jpg
    - **Important**: All UI development must follow these designs

## Technologies and Libraries

### Core Technologies

- **Kotlin Multiplatform**: Version 2.2.0
- **Compose Multiplatform**: Version 1.8.2
- **Ktor**: Version 3.2.0 (Server framework)
    - Server: Core, Netty, Test Host
    - Client: Core, OkHttp (Android/JVM), Darwin (iOS)
- **Google Cloud**: Version 26.40.0
    - Firestore: Database service
    - Scheduler: Task scheduling service

### Android-specific

- Android SDK: compileSdk 35, minSdk 24, targetSdk 35
- AndroidX libraries:
    - Activity Compose: 1.10.1
    - Lifecycle: 2.9.1
    - Core KTX: 1.16.0
    - AppCompat: 1.7.1
    - ConstraintLayout: 2.2.1

### Server-specific

- Ktor Server with Netty engine
- Logback: 1.5.18 (Logging)

### Testing

- Kotlin Test
- JUnit: 4.13.2
- AndroidX Test Extensions: 1.2.1
- Espresso: 3.6.1
- Ktor Server Test Host

### Build Tools

- Gradle with the following plugins:
    - Android Application/Library
    - Compose Multiplatform
    - Kotlin Multiplatform
    - Ktor
    - Shadow: 8.3.6 (For creating fat/uber JARs)

## Setup Instructions

### Prerequisites

- JDK 11 or higher
- Android Studio Arctic Fox or higher
- Xcode 13 or higher (for iOS development)

### Building the Project

1. Clone the repository
2. Open the project in Android Studio or IntelliJ IDEA
3. Sync Gradle files
4. Build the project using the Gradle task: `./gradlew build`

### Running the Applications

- **Android App**: Run the `composeApp` module on an Android device or emulator
- **iOS App**: Open the Xcode project in the `iosApp` directory and run on an iOS device or simulator
- **Server**: Run the `server` module using the Gradle task: `./gradlew :server:run`

## Design Guidelines

### UI Design

The UI design for the application is specified in the `/designs` folder. Currently, this includes:

- `Main-Screen.jpg`: Design for the main inventory tracking screen
- `Add-Edit.jpg`: Design for adding and editing inventory items

**Important**: All UI development must strictly follow these design specifications. Before making any changes to the UI,
consult the design files in the `/designs` folder. Additional design files may be added in the future.

### Coding Standards

- Follow Kotlin coding conventions
- Use meaningful names for classes, functions, and variables
- Write unit tests for all new functionality
- Document public APIs
