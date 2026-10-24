# DZ PhantomKit

A branded dashboard app for a **rootless** Kali NetHunter setup on Android.
Target device reference: POCO F5 Pro (arm64-v8a), but it will run on any
arm64 device on API 26+.

## What this actually is

This is intentionally a **front-end**, not a reimplementation of NetHunter's
tools. There is no practical way to recompile Termux + the full Kali
userland + the NetHunter-KeX client into one custom APK without that
project's own native build chain (per-architecture bootstrap binaries, NDK,
etc.) — that's a multi-repo effort, not a single-file build.

What DZ PhantomKit does instead:
1. Shows install steps and puts the official NetHunter-rootless install
   commands on the clipboard.
2. Launches the environment by sending commands to **Termux** through its
   own documented automation API (`RunCommandService` — the same mechanism
   Termux:Tasker and Termux:Widget use). No tools, exploits, or chroot setup
   code are bundled or reimplemented here.

The actual Kali toolset still comes from the real, open-source project:
https://www.kali.org/docs/nethunter/nethunter-rootless/

## One-time setup on the phone

1. Install **Termux** (F-Droid build, not the stale Play Store one) and
   **Hacker's Keyboard**.
2. In Termux, edit `~/.termux/termux.properties`, uncomment and set:
   ```
   allow-external-apps=true
   ```
   then run `termux-reload-settings`.
3. Install DZ PhantomKit's APK and grant it any prompted permissions.
4. Open DZ PhantomKit → **Copy install commands** → open Termux → paste →
   Enter. This runs the official installer, so expect it to download a few
   hundred MB of Kali packages.
5. Once that finishes, **Launch CLI** / **Launch Desktop (KeX)** from the
   app will drive Termux directly.

## Building the APK

```bash
# from the project root
./gradlew assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
```

If `gradlew` is missing (it wasn't bundled with these sources), generate it
once with Android Studio by opening the project, or run:
```bash
gradle wrapper --gradle-version 8.7
```
Requires: JDK 17, Android SDK (compileSdk 34), Android Gradle Plugin 8.5+.

## Licensing / naming — please keep this in mind

- **Termux** is GPLv3. If you redistribute a modified Termux build, you must
  keep it open source.
- **Kali NetHunter** components are a mix of GPL/Apache-licensed code, but
  "Kali", "NetHunter", and "Offensive Security" are trademarks of OffSec —
  don't use those names/logos in your own app's branding or store listing,
  even though the underlying code is open.
- This project's own code (this app) is yours to license however you like;
  it just talks to Termux, it doesn't contain any Kali/NetHunter source.

## Notes

- All on-device strings are English-only, per your usual preference for
  scripts/apps.
- No wireless injection / monitor-mode tooling is possible without root —
  that's a kernel-level limitation of the rootless edition itself, not
  something this app can work around.
