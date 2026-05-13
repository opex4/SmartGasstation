# SmartGasstation

## Project Overview

SmartGasstation is a native Android application built with Kotlin. It helps users track vehicle refueling records, calculate average fuel consumption, and find the best gas stations based on fuel type and consumption data.

### Features
- Record refuels (fuel amount + odometer readings)
- View, edit, and delete refueling history
- Calculate average fuel consumption (L/100km)
- Export/import data as TXT, XLS, or PDF
- Network integration with a Gas Station API
- Demonstrates Kotlin Coroutines and Java Threads for background tasks

## Tech Stack

- **Language**: Kotlin (JVM Target 11)
- **Framework**: Android SDK (Min SDK 26, Target SDK 35)
- **UI Architecture**: MVVM (ViewModel + LiveData)
- **Database**: Room (SQLite)
- **Networking**: Retrofit 2 + OkHttp + Gson
- **File Processing**: Apache POI (XLS), Android PdfDocument (PDF)
- **Build System**: Gradle with Kotlin DSL (`*.gradle.kts`) and Version Catalogs

## Project Structure

```
SmartGasstation/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── java/com/example/smartgasstation/
│       │   ├── adapters/        # RecyclerView adapters
│       │   ├── data/            # Room DB entities, DAOs, API models
│       │   ├── filemanager/     # TXT, XLS, PDF export/import
│       │   ├── multithreading/  # Coroutines and Thread managers
│       │   ├── network/         # Retrofit API definitions
│       │   ├── viewModels/      # MainVM, AddRefuelVM
│       │   ├── MainActivity.kt
│       │   └── AddRefuelActivity.kt
│       └── res/                 # XML layouts, drawables, menus
├── gradle/
│   └── libs.versions.toml       # Version catalog
├── build.gradle.kts
└── settings.gradle.kts
```

## Running in Replit

This is a **native Android app** — it is not a web application and cannot run directly in the Replit preview pane. To run or test the app, you need:

1. **Android Studio** (recommended) — open the project and run on an emulator or physical device
2. **Android SDK** — required to build the APK

### Building the APK (if Android SDK is available)

```bash
./gradlew assembleDebug
```

The "Build APK" workflow is configured to run this command. The output APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## User Preferences

- Follow existing Kotlin and Android conventions
- Use MVVM architecture for any new features
