# Universal APK Builder

A reusable GitHub Actions workflow template for building APKs from Android projects in GitHub Actions.

## Supported project types

- Native Android projects using Gradle (Kotlin/Java)
- Flutter projects
- React Native projects with an `android/` Gradle project

## Install it in a project repository

1. Copy `.github/workflows/universal-apk-builder.yml` into the target repository at the same path.
2. Ensure the repository includes the Android source code and Gradle wrapper files (`gradlew`, `gradle/wrapper/`).
3. Open the repository on GitHub.
4. Select **Actions** → **Universal APK Builder** → **Run workflow**.
5. Choose **auto** for project type, and **debug** for the first build.
6. When the run completes, open the workflow run and download the APK from **Artifacts**.

## Debug vs Release

- **Debug** is the recommended first test. Android debug APKs are generally signed with the debug key and can be installed for testing.
- **Release** runs the project's release build task. Most real projects need signing configuration and secrets before the resulting release APK is suitable for distribution. This template does not create or store a release keystore and does not upload to Google Drive.

## Project-specific requirements

- **Native Android:** a working Gradle Android project and wrapper.
- **Flutter:** `pubspec.yaml` and an `android/` directory.
- **React Native:** `package.json`, an `android/` directory, and a compatible lockfile/dependency setup.
- The project must be able to build on a clean Ubuntu GitHub Actions runner. Some projects need additional SDKs, environment variables, private package credentials, or custom build commands.

## Security

- Do not commit keystores, passwords, API keys, service-account JSON, or other secrets to source control.
- Configure any required values in **Settings → Secrets and variables → Actions**.
- This template intentionally does not access or modify any other repository.

## Notes

This is a generic starting point, not a guarantee that every Android repository builds without project-specific changes. Review the Actions log if a project needs a custom setup step.
