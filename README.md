# My Application

A native Android app built with Jetpack Compose, showcasing a multi-module, MVVM-ish architecture with two independent features: a **Country browser** and a **DoorDash-style restaurant list**. It's structured as a playground for modern Android patterns — Navigation 3, Hilt, Room, Retrofit/Flow, and a shared design-system core.

## Modules

```
MyApplication/
├── app/                     # Application shell: DI wiring, Room DB, top-level navigation, theme
├── core/                    # Shared UI components, navigation framework, networking, use-case base classes
└── features/
    ├── feature-country/     # Browse countries fetched from a REST API, search, and view details
    └── feature-doordash/    # Browse nearby restaurants, like/unlike them (persisted locally)
```

- **`app`** depends on `core`, `feature-country`, and `feature-doordash`. It owns the `MyAppDatabase` (Room), app-wide Hilt modules (dispatchers, `SharedPreferences`, DB/DAO providers), and the bottom-nav host (`AppFeatureNavigation`) that stitches feature nav graphs together.
- **`core`** has no dependency on any feature. It provides:
  - Compose building blocks (`SearchBarView`, `ErrorView`, `LoadingViews`, `ListItemViews`, `ImageLoader`, `BottomNavigationBar`, etc.)
  - A `UIState<T>` sealed type (`Loading` / `Success` / `Error` / `Default`) that all feature ViewModels expose
  - `FlowUseCase`, a base class that turns a suspend/Flow data call into a `Progress → Success/Failed` stream (`UseCaseOutputWithStatus`)
  - Navigation 3 plumbing (`Navigator`, `NavigationState`, polymorphic route serialization)
  - A Retrofit `CallAdapterFactory` that maps network calls into `Flow` + typed `RetrofitException`
- **`feature-country`** and **`feature-doordash`** are self-contained feature modules (their own DI module, network service, repository, use cases, ViewModels, and screens), wired into the app only through navigation entry points.

## Features

### Country
- Fetches a list of countries from a REST endpoint (`CountryApis`) via `GetCountriesUseCase`.
- Renders an alphabetically-sectioned list (`CountryItem.Letter` / `CountryItem.CountryInfo`) with a live search bar that filters by name, code, or capital.
- Tapping a country opens a detail screen.
- List state survives process death via `SavedStateHandle`.

### DoorDash (restaurants)
- Fetches nearby restaurants by lat/lng (`GetNearByRestaurantsUseCase`) and cross-references each one against a local Room table (`LikedDb`/`LikedDao`) to show its liked/unliked status.
- Users can like/unlike a restaurant; the state is persisted locally and merged back into the list on next load.
- A detail screen (`GetRestaurantDetailUseCase`) shows more info per restaurant.

## Architecture

- **Pattern**: MVVM — Composable screens observe `StateFlow<UIState<T>>` exposed by Hilt `ViewModel`s; user actions call ViewModel functions; use cases talk to repositories.
- **DI**: [Hilt](https://dagger.dev/hilt/), with per-feature modules (`CountryModule`, `DoordashModule`) plus app-level providers in `AppModule`.
- **Navigation**: [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) with type-safe, `@Serializable` route keys and a custom `Navigator`/`NavigationState` layer in `core` for a shared bottom-nav + top-app-bar shell.
- **Networking**: Retrofit + OkHttp + Gson, with a custom call adapter (`FlowErrorHandlingCallAdapterFactory`) so repositories return `Flow<T>` and errors surface as typed `RetrofitException`s.
- **Persistence**: Room (`MyAppDatabase` in `app`, feature-owned DAOs like `LikedDao`) for local state such as liked restaurants.
- **Async**: Kotlin Coroutines/Flow throughout; `FlowUseCase` standardizes the loading/success/error lifecycle for every use case.
- **UI**: Jetpack Compose + Material 3, `kotlinx.collections.immutable` for stable list state, Coil for image loading.

## Configuration

API base URLs and API keys (`COUNTRY_SERVER_ENDPOINT`, `DOORDASH_SERVER_ENDPOINT`, `YELP_SERVER_ENDPOINT`, `YELP_API_TOKEN`) are injected as `BuildConfig` fields via the [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin). Define them in a local, git-ignored `secrets.properties` (or `local.properties`) at the project root:

```properties
COUNTRY_SERVER_ENDPOINT=https://your-country-api.example.com/
DOORDASH_SERVER_ENDPOINT=https://your-doordash-api.example.com/
YELP_SERVER_ENDPOINT=https://api.yelp.com/v3/
YELP_API_TOKEN=your-yelp-fusion-api-key
```

`YELP_API_TOKEN` is a real credential - never hardcode it in source, only in the git-ignored properties file.

## Tech stack

Kotlin · Jetpack Compose · Material 3 · Navigation 3 · Hilt · Room · Retrofit/OkHttp · Coil · Kotlin Coroutines/Flow · Kotlinx Serialization

## Requirements

- Android Studio (AGP 8.13.2)
- JDK 17
- minSdk 24, targetSdk/compileSdk 36

## Build & run

```
./gradlew :app:assembleDebug
```

Or open the project in Android Studio and run the `app` configuration.
