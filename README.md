# Android Patch Module (Android App + Patch Core)

This repository contains a real Android application (`app`) plus a reusable patch framework module (`patch-core`) for DEX-like bytecode transformation experiments.

## Modules
- `app`: Android application entrypoint (`MainActivity`) demonstrating runtime execution of the patch engine.
- `patch-core`: Kotlin patch framework with modular signatures, scanners, graph checks, and transformations.

## Architecture

```text
APK Input
  ↓
DEX Parser (IR iteration)
  ↓
Pattern Scanner (opcode/string/call anchors)
  ↓
Graph Matcher (branch/invoke/return control-flow shape)
  ↓
Patch Engine (module transform application)
  ↓
Patch Result (applied count + touched methods)
```

## Patch module contract
A patch module defines:
- name
- description
- version compatibility
- bytecode pattern signature
- patch transformation logic

Implemented example: `YouTubeSkipAdsModule` in `patch-core`.

## End-to-end test policy
- E2E tests are implemented in `patch-core/src/test/.../PatchPipelineE2ETest.kt`.
- Tests use real parser/scanner/graph matcher/engine/module components.
- No mocks or fakes.

## Run tests

```bash
gradle :patch-core:test --configure-on-demand
```

## Build debug APK

```bash
gradle :app:assembleDebug --configure-on-demand
```

Expected APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## CI workflow
- Runs `patch-core` tests on every push and pull request.
- Builds a debug APK and publishes it as a workflow artifact (`app-debug-apk`).

## Android app run
Open with Android Studio and run `app` on device/emulator.
