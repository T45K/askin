# Agent Notes

## Project Overview

This repository contains an Android MVP for a Japanese muscle-training rep tracker. The app records daily total repetitions per exercise, manages exercise and category masters, shows today's summary and history, and shares daily records through the Android sharesheet for X.

## Current Implementation Summary

- The Android app uses Kotlin, Jetpack Compose, Navigation Compose, Room, ViewModel, Flow/StateFlow, and repository/use-case layers.
- Source files live under `app/src/main/kotlin` and tests live under `app/src/test/kotlin`.
- The package, namespace, and application ID are `io.github.t45k.askin`.
- Room schema output is stored under `app/schemas/io.github.t45k.askin.data.local.AppDatabase`.
- Android Gradle Plugin is pinned to `9.0.0-alpha06` in the root `build.gradle.kts`.
- Generated text files were normalized to end with a trailing newline.

## Important Source Layout

- Main application entry points:
  - `app/src/main/kotlin/io/github/t45k/askin/MainActivity.kt`
  - `app/src/main/kotlin/io/github/t45k/askin/AskinApplication.kt`
  - `app/src/main/kotlin/io/github/t45k/askin/AskinApp.kt`
- Local database and seed data:
  - `app/src/main/kotlin/io/github/t45k/askin/data/local`
  - `app/src/main/kotlin/io/github/t45k/askin/data/local/seed/InitialMasterSeeder.kt`
- Domain logic:
  - `app/src/main/kotlin/io/github/t45k/askin/domain`
- UI screens and ViewModels:
  - `app/src/main/kotlin/io/github/t45k/askin/ui`
- Share integration:
  - `app/src/main/kotlin/io/github/t45k/askin/share/XShareLauncher.kt`
- Unit tests:
  - `app/src/test/kotlin/io/github/t45k/askin`

## Build and Verification

Use the following command for the current verification path:

```shell
gradle :app:testDebugUnitTest :app:assembleDebug
```

The command was run successfully after the package/source-layout migration with AGP `9.0.0-alpha06`.

## Notes for Future Agents

- Keep Kotlin production code under `app/src/main/kotlin`, not `app/src/main/java`.
- Keep Kotlin tests under `app/src/test/kotlin`, not `app/src/test/java`.
- Preserve the package name `io.github.t45k.askin` unless the user explicitly requests another migration.
- If Room entities or the database package changes, update or regenerate the schema path under `app/schemas`.
- This project is intended to remain a local-first Android MVP unless the user asks for cloud sync, authentication, or external API posting.
- X integration currently uses `Intent.ACTION_SEND` and the Android sharesheet; it does not use X API authentication.
