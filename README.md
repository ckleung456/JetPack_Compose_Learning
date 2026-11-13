# My Application

A native Android app built with Jetpack Compose, showcasing a multi-module architecture with three independent features: a **Country browser**, a **DoorDash-style restaurant list**, and a **Yelp-backed "Pizza & Beer" business search** with device-location support. It's structured as a playground for modern Android patterns — Navigation 3, Hilt, Room, Retrofit/Flow, Play Services location, and a layered set of shared core modules.

## Modules

```
MyApplication/
├── app/                     # Application shell: DI wiring, Room DB, top-level navigation, theme
├── core/
│   ├── domain/              # Pure contracts: Result/EmptyResult, DataError/Kind, UseCaseOutputWithStatus, LocationAddressProvider
│   ├── network/             # Retrofit FlowErrorHandlingCallAdapterFactory + typed RetrofitException
│   ├── presentation/        # Compose components, UIState, error mapping, location permission requester, Navigation 3 plumbing
│   ├── shared-lib/          # FlowUseCase, ObserveAsEvents, location permission helpers, utilities
│   └── location/            # FusedLocationAddressProvider (Play Services + Geocoder) and its Hilt LocationModule
└── features/
    ├── feature-country/     # Browse countries fetched from a REST API, search, and view details
    ├── feature-doordash/    # Browse nearby restaurants, like/unlike them (persisted locally)
    └── feature-business/    # Search nearby pizza & beer spots on Yelp, filter/search, view details
```

### Dependency rules

- **`app`** depends on `core:location`, `core:presentation`, and all three features. It owns the `MyAppDatabase` (Room), app-wide Hilt modules (`AppModule`/`AppQualifiers`: dispatchers, `SharedPreferences`, DB/DAO providers), and the bottom-nav host (`AppFeatureNavigation`) that stitches the Country, DoorDash, and Business nav graphs together (start route: `CountryRoute.Countries`).
- **Features** each depend on `core:domain`, `core:presentation`, `core:network`, and `core:shared-lib`. Features never depend on each other; they are wired into the app only through navigation entry points.
- **`core:location`** depends on `core:domain` and `core:shared-lib`.
- **`core:network`**, **`core:presentation`**, and **`core:shared-lib`** expose `core:domain` via `api`. `core:domain` depends on nothing.

### Core modules

- **`core:domain`** — framework-free contracts:
  - Typed `Result<T, E : Error>` (`Success` / `Error`) and `EmptyResult<E>`
  - `DataError` with a `Kind` (`NETWORK` / `CONVERSION` / `HTTP` / `UNEXPECTED`)
  - `UseCaseOutputWithStatus` (`Progress → Success/Failed`)
  - `LocationAddressProvider` interface
- **`core:network`** — a Retrofit `CallAdapterFactory` (`FlowErrorHandlingCallAdapterFactory`) that maps network calls into `Flow` + typed `RetrofitException`.
- **`core:presentation`** — Compose building blocks (`SearchBarView`, `ErrorView`, `LoadingViews`, `ListItemViews`, `ImageLoader`, `BottomNavigationBar`, `AppTopBar`, `UIStatefulContent`, etc.), the `UIState<T>` sealed type (`Loading` / `Success` / `Error` / `Default`), error-to-UI mapping, `rememberLocationPermissionRequester`, and Navigation 3 plumbing (`Navigator`, `NavigationState`, polymorphic route serialization).
- **`core:shared-lib`** — `FlowUseCase` (turns a suspend/Flow data call into a `UseCaseOutputWithStatus` stream), `ObserveAsEvents` for one-off UI events, location permission/enabled helpers, and general utilities.
- **`core:location`** — `FusedLocationAddressProvider`, an implementation of `LocationAddressProvider` that gets the current location from the Fused Location Provider and turns it into an address with `Geocoder`, provided through Hilt's `LocationModule`.

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

### Business (Yelp "Pizza & Beer")
- `SearchPizzaAndBeerUseCase` runs the Yelp `businesses/search` query for pizza and for beer at the same time and combines the results. If only one search fails, the other is still shown and the failed category is reported in `failedCategories`. The use case fails only if both searches fail.
- `BuildBusinessSectionsUseCase` groups the results into sections by category, filtered by `BusinessFilter` (All / Pizza / Beer) and the search query. It removes duplicates and sorts each section by name.
- The address header lets the user use the device's current location (`GetCurrentAddressUseCase` → `LocationAddressProvider`) or type an address in `AddressDialog`. The default address is `1000 Montgomery Street, San Francisco, CA`.
- Supports pull-to-refresh and retry. Tapping a business opens a detail screen (`GetBusinessDetailUseCase`, Yelp `businesses/{id}`).
- The list screen follows a state/action/event (MVI-style) contract: `BusinessListState`, `BusinessListAction`, `BusinessListEvent` consumed via `ObserveAsEvents`.

## Architecture

- **Pattern**: Country and DoorDash use MVVM: Composable screens observe `StateFlow<UIState<T>>` from Hilt `ViewModel`s, user actions call ViewModel functions, and use cases talk to repositories. Business uses an MVI-style state/action/event contract on the same foundations.
- **DI**: [Hilt](https://dagger.dev/hilt/), with a module per feature (`CountryModule`, `DoordashModule`, `BusinessModule`), `LocationModule` in `core:location`, and app-level providers in `AppModule`.
- **Navigation**: [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) with type-safe, `@Serializable` route keys and a custom `Navigator`/`NavigationState` layer in `core:presentation` for a shared bottom-nav + top-app-bar shell.
- **Networking**: Retrofit + OkHttp + Gson, with a custom call adapter (`FlowErrorHandlingCallAdapterFactory`) so repositories return `Flow<T>` and errors surface as typed `RetrofitException`s. The Business feature adds an `AuthInterceptor` that sends the Yelp API key as a `Bearer` token.
- **Errors**: typed `Result<T, E : Error>` / `DataError` in `core:domain` for non-network failures such as location lookup.
- **Location**: Play Services Fused Location Provider + `Geocoder` (`core:location`). Runtime permission is requested with `rememberLocationPermissionRequester` (`core:presentation`).
- **Persistence**: Room (`MyAppDatabase` in `app`, feature-owned DAOs like `LikedDao`) for local state such as liked restaurants.
- **Async**: Kotlin Coroutines/Flow throughout; `FlowUseCase` standardizes the loading/success/error lifecycle for every use case.
- **UI**: Jetpack Compose + Material 3, `kotlinx.collections.immutable` for stable list state, Coil for image loading.

## Configuration

API base URLs and API keys are injected as `BuildConfig` fields via the [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin):

| Key | Used by |
| --- | --- |
| `COUNTRY_SERVER_ENDPOINT` | `feature-country` |
| `DOORDASH_SERVER_ENDPOINT` | `feature-doordash` |
| `YELP_SERVER_ENDPOINT` | `feature-business` |
| `YELP_API_TOKEN` | `feature-business` (`AuthInterceptor`) |

Define them in a local, git-ignored `secrets.properties` (or `local.properties`) at the project root:

```properties
COUNTRY_SERVER_ENDPOINT=https://your-country-api.example.com/
DOORDASH_SERVER_ENDPOINT=https://your-doordash-api.example.com/
YELP_SERVER_ENDPOINT=https://api.yelp.com/v3/
YELP_API_TOKEN=your-yelp-fusion-api-key
```

`YELP_API_TOKEN` is a real credential - never hardcode it in source, only in the git-ignored properties file.

### Permissions

The app declares `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_COARSE_LOCATION`, and `ACCESS_FINE_LOCATION`. Location is requested at runtime only when the user chooses "use my location" in the Business feature; if it is denied, a message is shown and the user can still type an address instead.

## Tech stack

| Area | Library | Version |
| --- | --- | --- |
| Language | Kotlin | 2.2.0 |
| Build | Android Gradle Plugin | 8.13.2 |
| UI | Jetpack Compose (BOM) + Material 3 | 2025.12.01 |
| Navigation | Navigation 3 | 1.0.0 |
| DI | Hilt | 2.57.2 |
| Persistence | Room | 2.6.1 |
| Networking | Retrofit / OkHttp | 3.0.0 / 4.10.0 |
| Images | Coil | 3.4.0 |
| Async | Kotlin Coroutines (+ Play Services) | 1.11.0 |
| Location | Play Services Location | 21.4.0 |
| Serialization | Kotlinx Serialization | 1.9.0 |

## Requirements

- Android Studio (AGP 8.13.2)
- JDK 17
- minSdk 24, targetSdk/compileSdk 36

## Build & run

```
./gradlew :app:assembleDebug
```

Or open the project in Android Studio and run the `app` configuration.
