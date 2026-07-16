# InStockTracker

InStockTracker is a Kotlin Multiplatform (KMP) project for tracking inventory / product availability across platforms.

## Modules

- **composeApp** — Compose Multiplatform UI and Android-specific implementations (Jetpack Compose, Material 3,
  Navigation Compose)
- **iosApp** — Xcode project and iOS entry points
- **server** — Ktor server (Netty), REST endpoints, logging (Logback)
- **shared** — Common code (`commonMain`) + platform-specific implementations (Android, iOS, JVM); models, networking,
  logic. Includes a `revenueCatMain` source set for billing.
- **designs** — UI mockups (`Main-Screen.jpg`, `Add-Edit.jpg`)

## Key Technologies

- Kotlin Multiplatform 2.3.20, Compose Multiplatform 1.9.0-rc01
- Ktor 3.2.3 (server and client)
- Google Cloud (Firestore, Scheduler; libraries-bom 26.64.0), Firebase (bom 34.5.0, Admin SDK 9.5.0), Google ID token
  auth on the server
- RevenueCat for billing/subscriptions (purchases 10.6.1, purchases-kmp 3.0.3)
- AndroidX stack for Android (AGP 8.10.0); Kotlin Test / JUnit / Espresso for testing

## Build & Run

- Build: `./gradlew build`
- Android app: run `composeApp` in Android Studio/IntelliJ on a device/emulator
- iOS app: open `iosApp` in Xcode and run on simulator/device
- Server: `./gradlew :server:run`

## Conventions

- Follow Kotlin coding conventions and document public APIs
- Adhere strictly to the designs in `/designs` when implementing UI
- Add tests for new functionality

## Skills

Project-specific skills live in `.claude/skills/` (imported from `.junie/skills/`) and are invocable via the Skill tool.
Three families:

- **Kotlin / KMP**: architecture review, feature implementation, state management, modularization, data layer, refactor
  safety, KMP bridges, deep links, Compose Multiplatform UI, adaptive resources, navigation, testing, code review,
  bugfix, Gradle governance.
- **Android**: `android-architecture` — official Android architecture recommendations (layered UI/domain/data,
  ViewModel + UI state, UDF, lifecycle effects, DI, testing, naming) with priority levels.
- **RevenueCat**: integrate, paywall, purchase flow, entitlements gate, identify user, customer center, migrate, testing
  setup, troubleshoot, charts, status, project creation.

Prefer the matching skill when working on a related task.

## Reference / Plans

Design and setup docs live in `.junie/plans/`:

- `.junie/plans/track-items-and-subscription-sync.md` — tracking items + subscription sync design
- `.junie/plans/REVENUECAT_SETUP.md` — RevenueCat setup notes
- `.junie/plans/PAYWALL_IMPLEMENTATION.md` — paywall implementation plan

Project guidelines source: `.junie/guidelines.md`. See also `DOCUMENTATION.md` at the project root (if present) for
deeper detail.
