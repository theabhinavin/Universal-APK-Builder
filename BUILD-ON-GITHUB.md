# AB.M. Scan — GitHub Actions APK build

This source archive is prepared for the `theabhinavin/Universal-APK-Builder` repository on branch `build-abm-scan`.

- Upload/extract these project files at the **repository root** (not inside an extra nested folder).
- Choose **Actions → Universal APK Builder → Run workflow**.
- Branch: `build-abm-scan`; Project type: `native-android`; Build variant: `debug`.
- When the run completes, download the `apk-native-android-debug-*` artifact and extract the APK.

The project did not include a Gradle wrapper or debug keystore. The builder workflow now installs Gradle 9.3.1 directly, and this archive uses the standard generated debug signing key. A release APK requires a separately configured release keystore and credentials.
