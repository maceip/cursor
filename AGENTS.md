# AGENTS.md

## Cursor Cloud specific instructions

This repo is a single-module **Android** app built with Gradle (Kotlin DSL) and Jetpack Compose.
- App module: `app/` · application id / package: `com.example.cursor` · entry point: `MainActivity`.
- Toolchain: Gradle `9.1.0` (wrapper), Android Gradle Plugin `9.0.1`, Kotlin `2.3.20`, `compileSdk`/`targetSdk` 36, `minSdk` 24.

### Environment (already provisioned in the VM snapshot)
These are baked into the VM image; the startup update script does not reinstall them:
- **JDK 17** at `/usr/lib/jvm/java-17-openjdk-amd64` (the build needs a 17 toolchain). `JAVA_HOME` is exported in `~/.bashrc`.
- **Android SDK** at `~/android-sdk` (`platform-tools`, `platforms;android-36`, `build-tools;36.0.0`, plus `emulator` + `system-images;android-30;default;x86_64`). `ANDROID_HOME`/`ANDROID_SDK_ROOT` are exported in `~/.bashrc`, and `~/android-sdk` is recorded in the gitignored `local.properties` (`sdk.dir=...`).
- Interactive shells pick up the above from `~/.bashrc`. The Gradle wrapper also works in a bare non-interactive shell because the launcher tolerates the system default JDK and reads the SDK path from `local.properties`.

### Standard commands
Run from the repo root (see `app/build.gradle.kts` for the full config):
- Build (dev/debug): `./gradlew assembleDebug` → APK at `app/build/outputs/apk/debug/app-debug.apk`
- Unit tests: `./gradlew testDebugUnitTest`
- Lint: `./gradlew lintDebug` (report: `app/build/reports/lint-results-debug.html`)

### Running / testing an APK — use the remote emulator service (preferred)
The cloud VM has **no KVM / nested virtualization**, so the bundled Android emulator only runs in software (TCG) mode: it boots very slowly (minutes) and SystemUI frequently ANRs. Do **not** rely on the local emulator for routine runs.

Instead, run/test APKs on the private remote emulator farm at `https://emu.devkeys.net`. Authentication uses the **`EMU_API_TOKEN`** secret, which is injected as an environment variable — never hardcode the token value or commit it to any file.

Quick health check: `curl -s https://emu.devkeys.net/healthz` (expects `{"ok":true,"emulator_ready":true,...}`).

Run an APK (token read from the env var, never printed):
```bash
JOB=$(curl -s -H "Authorization: Bearer $EMU_API_TOKEN" \
  -F apk=@app/build/outputs/apk/debug/app-debug.apk \
  -F run_seconds=60 \
  https://emu.devkeys.net/run | python3 -c 'import sys,json;print(json.load(sys.stdin)["job_id"])')

# poll until status is "completed" (look for "activity_started":true)
curl -s -H "Authorization: Bearer $EMU_API_TOKEN" https://emu.devkeys.net/jobs/$JOB

# artifacts
curl -s -H "Authorization: Bearer $EMU_API_TOKEN" https://emu.devkeys.net/jobs/$JOB/logs        # logcat
curl -s -H "Authorization: Bearer $EMU_API_TOKEN" -o screen.mp4 https://emu.devkeys.net/jobs/$JOB/screen.mp4
```
