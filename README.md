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
