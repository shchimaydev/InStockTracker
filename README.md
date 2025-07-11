# InStockTracker

This is a Kotlin Multiplatform project targeting Android, iOS, Server.

## Project Structure

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - `commonMain` is for code that's common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
      For example, if you want to use Apple's CoreCrypto for the iOS part of your Kotlin app,
      `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you're sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* `/server` is for the Ktor server application.

* `/shared` is for the code that will be shared between all targets in the project.
  The most important subfolder is `commonMain`. If preferred, you can add code to the platform-specific folders here
  too.

* `/designs` contains UI design mockups and specifications that must be followed for all UI development.
  **Important**: Always consult these designs before making any UI changes.

## Documentation

For detailed information about the project, including setup instructions, architecture, technologies used, and development guidelines, please refer to the [DOCUMENTATION.md](DOCUMENTATION.md) file.

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…


## Deplayment

Google Cloud command to deploy to AppEngine.

`gcloud app deploy build/libs/server-all.jar --project instocktracker-464721`