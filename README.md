# Android Patch Module (Android App + Patch Core)

This repository contains a real Android application (`app`) plus a reusable patch framework module (`patch-core`) for DEX-like bytecode transformation experiments.

## Modules
- `app`: Android application entrypoint (`MainActivity`) demonstrating installed-app selection and patch execution.
- `patch-core`: Kotlin patch framework with modular signatures, scanners, graph checks, and transformations.

## Architecture

```text
Installed Apps
  ↓
Target Selector (package name)
  ↓
APK Input / DEX IR
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

## Installed app selection flow
- `PatchTargetSelector` chooses a patch target from discovered installed apps using package name.
- `PatchWorkflow` composes selection + patch execution into one use case.
- `MainActivity` loads installed apps from `PackageManager`, lets the user pick one, then patches the selected app model.

## Patch module contract
A patch module defines:
- name
- description
- version compatibility
- bytecode pattern signature
- patch transformation logic

Implemented example: `YouTubeSkipAdsModule` in `patch-core`.

## End-to-end test policy
- E2E tests are implemented in `patch-core/src/test/.../e2e`.
- Tests use real parser/scanner/graph matcher/engine/module/workflow components.
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

## CI workflows
- `tests.yml`: runs `patch-core` tests on every push and pull request.
- `build-apk.yml`: separate APK workflow that first runs tests, then builds and uploads `app-debug.apk` as artifact `app-debug-apk`.

## Android app run
Open with Android Studio and run `app` on device/emulator.
