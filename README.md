# AB.M. Scan — Android Privacy & Security Audit

**Official Name:** AB.M. Scan  
**Brand Monogram:** ABM  
**Brand Owner & Signature:** ABM — Abhinav Mishra  
**Category:** Android Privacy & Security Audit  

---

## Overview

**AB.M. Scan** is a production-grade, privacy-first Android application designed to give users complete transparency into app permissions, sensor access, installation origins, and device security posture without exaggerated claims or simulated malware alerts.

The application operates **strictly within legitimate Android platform boundaries**, using documented Android APIs (`PackageManager`, `AppOpsManager`, `DevicePolicyManager`, `AccessibilityManager`, `KeyguardManager`, `Build`, and `Settings`).

---

## Key Privacy Principles

1. **100% Offline-First**: No internet permission is requested or used. All inspection logic, database storage, and report generation execute locally on the device.
2. **Zero Telemetry**: No analytics SDKs, advertising libraries, or external telemetry hooks.
3. **No Account Required**: Immediate audit capabilities with zero registration or credentials required.
4. **Data Sovereignty**: Local scan snapshots remain in on-device SQLite (Room) storage until explicitly wiped or shared by the user.

---

## Stalkerware & Spyware Detection Engine

AB.M. Scan features an evidence-based surveillance detection engine tailored for identifying potential stalkerware, covert monitoring tools, and elevated persistence vectors:

1. **Accessibility Service Risk Detection**: Identifies third-party applications with active accessibility service registrations that can intercept keystrokes and on-screen content (+40 threat score).
2. **Active Device Administrator Detection**: Detects non-system applications holding device administrator privileges that prevent normal uninstallation and enforce security overrides (+30 threat score).
3. **Installation Source Analysis**: Resolves installer packages (`PackageManager.getInstallSourceInfo`), highlighting sideloaded applications that bypassed Play Protect automated screening (+15 threat score).
4. **Hidden / Non-Launchable Application Detection**: Queries package manager launcher entries to identify third-party applications operating without a home screen launcher icon (+25 threat score).
5. **Surveillance Sensor Combination Analyzer**: Detects apps holding concurrent active grants for Background Location + Microphone recording + declared Internet access (+25 threat score).
6. **Local Offline IoC Database**: Bundled offline database matching known commercial stalkerware (e.g., mSpy, FlexiSPY, Hoverwatch, Cerberus, KidsGuard) via exact package identifiers and corroborated certificate SHA-256 fingerprints.
7. **Deterministic Threat Scoring**: Computes a transparent 0–100 threat score per application with explicit thresholds:
   - **0–19**: Low Concern
   - **20–39**: Review Recommended
   - **40–59**: Elevated Risk
   - **60–79**: High Risk — Investigate Promptly
   - **80–100**: Very High Risk
   Applications scoring 60 or higher trigger the warning: *"Potential stalkerware — multiple high-risk indicators detected. Manual investigation recommended."*
8. **Survivor Safety Advisory**: Critical guidance advising users that immediately altering settings or uninstalling stalkerware on a monitored device could alert an abuser, with links to survivor support resources (Coalition Against Stalkerware).

---

## 10 Core Audit Modules

1. **Device Overview**: Hardware manufacturer, model, Android OS version, API level, security patch level, screen lock status, and storage encryption.
2. **Installed Applications**: Searchable and filterable package inventory (system vs. user-installed, sensitive permissions, special privileges, and installer sources) with deep-links to Android App Info.
3. **Camera Privacy**: Audits declared and granted camera access rights, platform privacy indicators (Android 12+ green indicator), and shortcuts to the system Privacy Dashboard.
4. **Microphone Privacy**: Analyzes audio-recording grants and highlights background audio capabilities.
5. **Location Privacy**: Details precise (GPS) vs. approximate (network) location grants, with critical emphasis on background location permissions.
6. **Sensitive Permissions**: Categorizes 10 critical privacy domains: Camera, Microphone, Location, Contacts, Phone/Call Logs, SMS, Calendar, Nearby Devices/Bluetooth, Body Sensors, and Notifications.
7. **Special Access**: Detects high-privilege system overrides including enabled Accessibility Services, Device Administrators, Active VPN tunnels, and Notification Listeners.
8. **Unknown Installation Sources**: Flags non-Play Store and sideloaded packages with balanced, evidence-based risk explanations.
9. **Security Posture**: Checks screen lock configuration, developer options, USB debugging (ADB), and non-privileged root heuristics.
10. **Scan History**: Persists the single latest scan record in local Room storage, enabling instant resume, diffing against previous status, and one-tap history purging.

---

## Risk Scoring Formula

The device security score ranges from **15 to 100** based on transparent, explainable deductions:
- **Base Score**: 100 (Optimal Security Posture)
- **Critical Finding**: -25 pts (e.g., SU binary heuristics detected)
- **High Severity**: -12 pts (e.g., No screen lock protection, sideloaded app with camera + microphone + background location)
- **Medium Severity**: -6 pts (e.g., Developer options enabled, active accessibility service, sideloaded app)
- **Low Severity**: -2 pts (e.g., Sensitive background location grant on system app)
- **Informational**: 0 pts (Platform transparency notices)

---

## Architecture & Technology Stack

- **UI**: Jetpack Compose, Material 3, dynamic theme tokens
- **Architecture**: MVVM with unidirectional data flow (`StateFlow`, `collectAsStateWithLifecycle`)
- **Persistence**: AndroidX Room Database 2.6.1 + KSP
- **Serialization**: Moshi Kotlin
- **Reporting**: Structured JSON export and Android system share sheet (`Intent.ACTION_SEND` with `FileProvider`)
- **Testing**: Robolectric and JUnit 4

---

## Building and Verification

To compile the debug APK:
```bash
gradle assembleDebug
```

To run local unit and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```
