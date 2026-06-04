# RwaziHomeWork

Android notes app built with Kotlin, Jetpack Compose, Room, and Hilt.

## Overview

This project is a simple notes application that supports:

- Creating notes
- Editing note text
- Deleting notes (swipe to dismiss)
- Searching notes by text
- Sorting notes by newest or oldest
- Incremental list loading (pagination-style display)

On first launch, the app ensures a minimum set of default notes (50) for testing scrolling and pagination behavior.

## Tech Stack

- Kotlin 2.2.10
- Android Gradle Plugin 9.2.1
- Gradle 9.4.1 (wrapper)
- Jetpack Compose (Material 3)
- Room (local persistence)
- Hilt (dependency injection)
- AndroidX DataStore (legacy migration source)
- Coroutines + Flow

## Requirements

- macOS, Linux, or Windows
- Android Studio (recent stable version)
- Android SDK with:
  - minSdk 24
  - targetSdk 36
  - compileSdk 36
- JDK 11

## Getting Started

1. Clone and open the project in Android Studio.
2. Let Gradle sync complete.
3. Run the `app` configuration on an emulator or device.

## Build and Run (CLI)

From the repository root:

```bash
./gradlew assembleDebug
```

Install to a connected device/emulator:

```bash
./gradlew installDebug
```

## Testing

Run local unit tests:

```bash
./gradlew testDebugUnitTest
```

Run instrumentation tests (requires device/emulator):

```bash
./gradlew connectedDebugAndroidTest
```

## Architecture

This app follows a layered structure:

- `ui`: Compose UI and `HomeViewModel`
- `domain`: model, repository contract, and use cases
- `data`: Room entities/DAO/database and repository implementation
- `di`: Hilt modules for database and repository bindings

High-level flow:

1. UI emits user actions to `HomeViewModel`.
2. ViewModel invokes domain use cases.
3. Use cases call `NotesRepository` contract.
4. `RoomNotesRepository` persists/fetches notes using Room.
5. Notes stream back to UI through `Flow` and `StateFlow`.

## Data Notes

- Main table: `notes`
- Entity: `NoteEntity(id, text, createdAt, backgroundColorHex)`
- Legacy migration path: DataStore JSON payload is parsed and migrated to Room when the database is empty.

## Project Structure

```text
app/src/main/java/com/example/rwazihomework/
  data/
    local/                  # Room database, DAO, entity
    repository/             # Repository implementation and legacy parser
  di/                       # Hilt modules
  domain/
    model/                  # Domain models
    repository/             # Repository contract
    usecase/                # Application use cases
  ui/home/                  # ViewModel and UI helpers
  MainActivity.kt           # Main Compose screen
  RwaziHomeWorkApp.kt       # Hilt application class
```

## Useful Commands

```bash
./gradlew clean
./gradlew lint
./gradlew assembleRelease
```

## License

No license file is currently included in this repository.
