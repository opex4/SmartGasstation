# SmartGasstation

## Project Overview

SmartGasstation is a native Android application built with Kotlin. It helps users track vehicle refueling records, calculate average fuel consumption, and find the best gas stations via a remote API.

### Features
- Record refuels (fuel amount + odometer readings)
- View, edit, and delete refueling history
- Calculate average fuel consumption (L/100km)
- Export/import data as TXT, XLS, or PDF
- Network integration with a Gas Station API (best station search)
- Demonstrates Kotlin Coroutines and Java Threads for background tasks

## Tech Stack

- **Language**: Kotlin (JVM Target 11)
- **Framework**: Android SDK (Min SDK 26, Target SDK 35)
- **UI Architecture**: Clean MVVM (ViewModel + LiveData + Use Cases)
- **Dependency Injection**: Hilt (Dagger)
- **Database**: Room (SQLite)
- **Networking**: Retrofit 2 + OkHttp + Gson
- **File Processing**: Apache POI (XLS), Android PdfDocument (PDF)
- **Build System**: Gradle with Kotlin DSL (`*.gradle.kts`) and Version Catalogs

## Architecture

The project follows **Clean MVVM** with three distinct layers:

```
UI Layer          → Activities observe ViewModel state only. No business logic.
Domain Layer      → Use Cases contain all business rules and validation.
Data Layer        → Repositories are pure data access (DB + network). No logic.
```

DI is provided by **Hilt** — ViewModels and Use Cases receive all dependencies via constructor injection.

## Project Structure

```
SmartGasstation/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── java/com/example/smartgasstation/
│       │   ├── adapters/              # RecyclerView adapters
│       │   ├── data/
│       │   │   ├── db/                # AppDatabase, RefuelDao, RefuelRecordEntity
│       │   │   ├── api/               # GasStationApi, BestStationResponse, StationIdsResponse (DTO)
│       │   │   ├── RefuelRepository.kt        # Pure DB access (insert/update/delete/get)
│       │   │   └── repository/
│       │   │       └── GasStationRepository.kt # Network API access
│       │   ├── di/                    # Hilt DI modules
│       │   │   ├── AppModule.kt       # DB, DAO, FileManager
│       │   │   ├── NetworkHiltModule.kt # OkHttpClient, Retrofit, GasStationApi, CacheControlInterceptor
│       │   │   └── RepositoryModule.kt  # RefuelRepository, GasStationRepository
│       │   ├── domain/
│       │   │   └── usecase/           # Business logic & validation
│       │   │       ├── AddRefuelUseCase.kt
│       │   │       ├── DeleteRefuelUseCase.kt
│       │   │       ├── UpdateRefuelUseCase.kt
│       │   │       ├── ClearHistoryUseCase.kt
│       │   │       ├── GetRefuelRecordsUseCase.kt
│       │   │       ├── FindBestStationUseCase.kt
│       │   │       ├── ExportToTxtUseCase.kt
│       │   │       ├── ExportToXlsUseCase.kt
│       │   │       ├── ExportToPdfUseCase.kt
│       │   │       ├── ImportFromTxtUseCase.kt
│       │   │       └── ImportFromXlsUseCase.kt
│       │   ├── filemanager/           # TXT, XLS, PDF export/import
│       │   ├── multithreading/        # CoroutineManager, ThreadManager
│       │   ├── network/               # (пусто — инфраструктура перенесена в di/)
│       │   ├── viewModels/
│       │   │   ├── MainVM.kt          # @HiltViewModel
│       │   │   └── AddRefuelVM.kt     # @HiltViewModel
│       │   ├── SmartGasstationApp.kt  # @HiltAndroidApp Application class
│       │   ├── MainActivity.kt        # @AndroidEntryPoint
│       │   └── AddRefuelActivity.kt   # @AndroidEntryPoint
│       └── res/                       # XML layouts, drawables, menus
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

## Running in Replit

This is a **native Android app** — it cannot run in the Replit preview pane. To build and run:

1. **Android Studio** (recommended) — open the project and run on an emulator or physical device
2. The "Build APK" workflow runs `./gradlew assembleDebug`
3. Output APK: `app/build/outputs/apk/debug/app-debug.apk`

## User Preferences

- Follow Clean MVVM architecture (UI → Domain → Data)
- Use Hilt for all dependency injection
- ViewModels must use @HiltViewModel + @Inject constructor
- Activities must be annotated with @AndroidEntryPoint
- All business logic belongs in Use Cases, not in Repositories or Activities
