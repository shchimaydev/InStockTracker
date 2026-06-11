---
name: android-architecture
description: Use when designing, implementing, or reviewing Android app architecture — layered architecture (UI / domain / data), UI layer + ViewModel design, unidirectional data flow, lifecycle-aware effects, dependency injection, StateFlow/UI state modeling, testing strategy, per-layer models, and naming conventions. Reference for the official Android architecture recommendations and their priority levels (strongly recommended / recommended / optional).
license: Apache-2.0
metadata:
  author: Android Developers (developer.android.com), adapted for InStockTracker
  version: "1.0.0"
  source: https://developer.android.com/topic/architecture/recommendations
---

# Android Architecture Recommendations

Reference for the official Google recommendations on Android app architecture. Use it to guide new feature design, to settle architecture debates, and as a checklist when reviewing Android-side code.

These are **recommendations, not strict requirements**. Adapt them to the app. Treat conflicts with an existing, deliberate project decision as a discussion point, not an automatic failure.

> **KMP note (InStockTracker):** This project is Kotlin Multiplatform. Most of these recommendations apply to the Android UI layer (`composeApp`) and to any Android-specific data sources. Shared business/data logic lives in `shared/commonMain`. When a recommendation says "data layer" or "repository," that often maps to shared code; when it says "ViewModel," "Compose," or "lifecycle," that is Android-side. Pair this skill with `kotlin-project-architecture-review` and `kotlin-data-kmp-data-layer` for the KMP-specific boundaries.

---

## Priority levels

Every recommendation below carries one of these strengths:

- **Strongly recommended** — Implement unless it clashes fundamentally with your approach.
- **Recommended** — Likely to improve your app.
- **Optional** — Can improve your app in certain circumstances.

---

## Layered architecture

A modern Android app architecture favors **separation of concerns**, **drives the UI from data models**, and follows the **single source of truth (SSOT)** and **unidirectional data flow (UDF)** principles.

| Recommendation | Strength | Notes |
|---|---|---|
| Use a clearly defined **data layer** | Strongly recommended | Exposes app data and contains most business logic. Create repositories even with a single data source. In small apps, place data-layer types in a `data` package/module. |
| Use a clearly defined **UI layer** | Strongly recommended | Displays data and is the primary point of user interaction. Jetpack Compose is the recommended toolkit. In small apps, place UI types in a `ui` package/module. |
| Expose application data via a **repository** | Strongly recommended | UI components (composables, ViewModels) must **not** interact directly with data sources — databases, DataStore, SharedPreferences, Firebase, GPS, Bluetooth, network status providers, etc. |
| Use **coroutines and flows** | Strongly recommended | Use them to communicate between layers. |
| Use a **domain layer** | Recommended (big apps) | Add use cases to reuse business logic across ViewModels or to simplify complex ViewModel logic. |

---

## UI layer

Displays application data and is the primary point of user interaction.

| Recommendation | Strength | Notes |
|---|---|---|
| Follow **unidirectional data flow (UDF)** | Strongly recommended | ViewModels expose UI state via the observer pattern and receive actions through method calls. |
| Use **Architecture Components ViewModels** | Strongly recommended | Handle business-logic calls, fetch data, and expose UI state. |
| Use **lifecycle-aware UI state collection** | Strongly recommended | Collect UI state with `collectAsStateWithLifecycle`. |
| **Do not send events** from ViewModel → UI | Strongly recommended | Process the event immediately in the ViewModel and cause a state update with the result. Model results as state, not one-shot UI events, wherever possible. |
| Use a **single-activity application** | Strongly recommended | Use Navigation to move between screens and to deep link, if the app has more than one screen. |
| Use **Jetpack Compose** | Strongly recommended | Recommended for new apps on phones, tablets, foldables, and Wear OS. |

**Lifecycle-aware state collection:**
```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

---

## ViewModel

Responsible for providing UI state and accessing the data layer.

| Recommendation | Strength | Notes |
|---|---|---|
| Keep ViewModels **independent of the Android lifecycle** | Strongly recommended | Don't hold references to lifecycle-related types. Don't pass `Activity`, `Fragment`, `Context`, or `Resources` as dependencies. If something needs a `Context`, evaluate whether it belongs in the data or UI layer. |
| Use **coroutines and flows** | Strongly recommended | Receive data with Kotlin flows; trigger actions with `suspend` functions inside `viewModelScope`. |
| Use ViewModels at **screen level** | Strongly recommended | Don't use them in reusable UI pieces. Use them in screen-level composables, in destinations, or in graphs (Jetpack Navigation). For complex reusable composables that need their own scope, use `rememberViewModelStoreOwner()`. |
| Use **plain state-holder classes** in reusable UI components | Strongly recommended | Enables state hoisting and external control of the component. |
| **Do not use `AndroidViewModel`** | Recommended | Use plain `ViewModel`. Move `Application`-context dependencies into the UI or data layer instead. |
| **Expose a UI state** | Recommended | Expose state through a single `uiState` property (multiple properties are fine for unrelated, independent data). |

**UI state modeling:**
- Make `uiState` a `StateFlow`.
- For data that comes from a stream, use `stateIn` with the `WhileSubscribed(5000)` sharing policy.
- For simpler cases, back it with a `MutableStateFlow` exposed as an immutable `StateFlow`.
- Define a `${Screen}UiState` **data class** that contains data, error, and loading signals — or a **sealed class/interface** when the states are mutually exclusive (e.g. `Loading`, `Success`, `Error`).

**Exposing UI state from a stream:**
```kotlin
@HiltViewModel
class BookmarksViewModel @Inject constructor(
    newsRepository: NewsRepository
) : ViewModel() {

    val feedState: StateFlow<NewsFeedUiState> =
        newsRepository
            .getNewsResourcesStream()
            .mapToFeedState(savedNewsResourcesState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewsFeedUiState.Loading
            )
    // ...
}
```

---

## Lifecycle

| Recommendation | Strength | Notes |
|---|---|---|
| Use **lifecycle-aware effects** in composables instead of overriding `Activity`/`Fragment` lifecycle callbacks | Strongly recommended | Don't override methods like `onResume` for UI tasks. |

Use the right tool for the job:
- `LifecycleStartEffect` — synchronous work tied to **start/stop**.
- `LifecycleResumeEffect` — synchronous work tied to **resume/pause**.
- `repeatOnLifecycle` — asynchronous work tied to lifecycle events.
- `collectAsStateWithLifecycle` — asynchronous data from flows, collected lifecycle-aware.

**Example — register/unregister around the started state:**
```kotlin
@Composable
fun LocationChangedEffect(
    locationManager: LocationManager,
    onLocationChanged: (Location) -> Unit
) {
    val currentOnLocationChanged by rememberUpdatedState(onLocationChanged)

    LifecycleStartEffect(locationManager) {
        val listener = LocationListener { newLocation ->
            currentOnLocationChanged(newLocation)
        }
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000L, 1f, listener,
            )
        } catch (e: SecurityException) {
            // TODO: Handle missing permissions
        }
        onStopOrDispose {
            locationManager.removeUpdates(listener)
        }
    }
}
```

---

## Handle dependencies

| Recommendation | Strength | Notes |
|---|---|---|
| Use **dependency injection** | Strongly recommended | Prefer **constructor injection** when possible. |
| **Scope to a component** when necessary | Strongly recommended | Scope a dependency to a container when the type holds shared mutable data, or is expensive to initialize and widely used. |
| Use **Hilt** | Recommended | Manual DI is fine for simple apps. Use Hilt for complex projects — multiple screens with ViewModels, WorkManager usage, or ViewModels scoped to the navigation back stack. |

---

## Models

| Recommendation | Strength | Notes |
|---|---|---|
| Create a **model per layer** in complex apps | Recommended | Keep each layer's model shaped for its own needs. Examples: a remote data source maps a network model to a simpler internal class; a repository maps DAO/entity models to simpler data classes; the ViewModel wraps data-layer models inside its `UiState` classes. |

---

## Testing

| Recommendation | Strength | Notes |
|---|---|---|
| **Know what to test** | Strongly recommended | Unless it's a "hello world" app, test it. Minimum: unit tests for ViewModels (including their flows), unit tests for data-layer entities (repositories and data sources), and UI navigation tests useful as CI regression checks. |
| **Prefer fakes to mocks** | Strongly recommended | Use real fake implementations as test doubles instead of mocking frameworks where practical. |
| **Test StateFlows** | Strongly recommended | Assert on the `.value` property whenever possible; rely on `WhileSubscribed` semantics. |

---

## Naming conventions (optional)

| Subject | Convention | Example |
|---|---|---|
| Methods | Verb phrase | `makePayment()` |
| Properties | Noun phrase | `inProgressTopicSelection` |
| Streams of data | `get{Model}Stream` (plural for lists) | `getAuthorStream(): Flow<Author>`, `getAuthorsStream(): Flow<List<Author>>` |
| Interface implementations | Meaningful name describing the impl | `OfflineFirstNewsRepository`, `InMemoryNewsRepository`; use `Default` prefix when there's no better name (`DefaultNewsRepository`); prefix fakes with `Fake` (`FakeAuthorsRepository`) |

---

## Quick review checklist

When designing or reviewing an Android feature, confirm:

- [ ] Data is exposed through a **repository**; no composable/ViewModel touches a data source directly.
- [ ] UI follows **UDF**: state flows down, events go up as method calls.
- [ ] State is collected with **`collectAsStateWithLifecycle`**.
- [ ] ViewModel holds **no** `Context`/`Activity`/`Resources`/lifecycle references and is **not** `AndroidViewModel`.
- [ ] UI state is a single `StateFlow<…UiState>`, with `WhileSubscribed(5000)` for streamed data.
- [ ] Results are modeled as **state**, not fire-and-forget events from the ViewModel.
- [ ] Lifecycle work uses **lifecycle-aware effects**, not overridden callbacks.
- [ ] Dependencies are **constructor-injected**; Hilt used when the project is complex enough to warrant it.
- [ ] A **domain layer / use cases** exist only where business logic is complex or reused.
- [ ] ViewModels and data-layer entities have **unit tests**; test doubles are **fakes**.
- [ ] Naming follows the conventions above where it adds clarity.

---

## How to use this skill

- **Designing a new screen/feature:** Walk the UI-layer and ViewModel tables top to bottom; model the `UiState` before writing composables.
- **Reviewing a PR:** Run the Quick review checklist; cite the relevant recommendation and its strength.
- **Settling a debate:** Quote the priority level. "Strongly recommended" carries real weight; "Optional" is a style preference, not a blocker.
- **In this KMP repo:** Apply UI/ViewModel/lifecycle guidance to `composeApp`; apply data-layer/repository/model guidance with `kotlin-data-kmp-data-layer`, keeping shared logic in `shared/commonMain`.

---

## References

- Recommendations for Android architecture: https://developer.android.com/topic/architecture/recommendations
- Guide to app architecture: https://developer.android.com/topic/architecture
- UI layer: https://developer.android.com/topic/architecture/ui-layer
- Domain layer: https://developer.android.com/topic/architecture/domain-layer
- Data layer: https://developer.android.com/topic/architecture/data-layer
- Compose UI architecture / architectural layering: https://developer.android.com/develop/ui/compose/architecture
