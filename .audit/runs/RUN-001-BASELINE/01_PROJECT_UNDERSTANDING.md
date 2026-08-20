# Project Understanding — RUN-001-BASELINE

Status: VERIFIED where cited; current source working tree is dirty before audit.

BenimGünlerim is a Turkish, single-user Android daily-life tracker. It uses Kotlin, Jetpack Compose/Material3, Hilt, Room and DataStore. The primary app module is supported by a macrobenchmark module and custom Detekt rules.

## Architecture and data flow

Compose screens and ViewModels call domain use cases/services, which use repositories and Room/DataStore. Local state covers tasks, routines, completion logs, daily state, preferences and game progression. Alarm/receiver components schedule reminders; boot/time-change paths restore scheduling. Export/import is local JSON. No server API, account, remote sync, payment, or tenant boundary was identified.

## Constraints and release context

`minSdk=26`, `targetSdk=36`, and a release variant with R8/resource shrinking are configured. A signed release APK was built in this environment. The repository has pre-existing unstaged/staged user changes and deleted documentation; this baseline evaluates the checked-out filesystem at `e5517418964d5e7b82216817fcc9a8b12079fd12` plus that working-tree state.

Confidence: CONFIRMED by E-01, E-02, E-03, E-07, E-12.
