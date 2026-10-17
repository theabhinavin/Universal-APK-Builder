package com.example.scanner

import android.content.Context
import android.provider.Settings
import com.example.domain.model.AppAuditItem
import com.example.domain.model.AuditCategory
import com.example.domain.model.AuditFinding
import com.example.domain.model.DeviceSecurityInfo
import com.example.domain.model.Severity
import com.example.domain.model.SpecialAccessItem
import java.util.UUID

class FindingEngine(private val context: Context) {

    fun generateFindings(
        deviceInfo: DeviceSecurityInfo,
        apps: List<AppAuditItem>,
        specialAccess: List<SpecialAccessItem>
    ): List<AuditFinding> {
        val findings = mutableListOf<AuditFinding>()

        // 1. Device Security Checks
        if (!deviceInfo.isDeviceSecure) {
            findings.add(
                AuditFinding(
                    id = "DEV_NO_LOCK",
                    title = "Screen Lock Security Not Configured",
                    severity = Severity.HIGH,
                    category = AuditCategory.SECURITY,
                    evidence = "KeyguardManager reports no PIN, password, or biometric lock protection active.",
                    explanation = "Anyone with physical possession of the phone has unrestricted access to your files, photos, contacts, and logged-in accounts.",
                    limitations = "Verified via official Android KeyguardManager API.",
                    recommendedAction = "Set a secure PIN, password, or fingerprint lock in Android Security settings.",
                    systemIntentAction = "android.settings.SECURITY_SETTINGS"
                )
            )
        }

        if (deviceInfo.isDeveloperOptionsEnabled) {
            findings.add(
                AuditFinding(
                    id = "DEV_OPTIONS_ENABLED",
                    title = "Developer Options Active",
                    severity = Severity.MEDIUM,
                    category = AuditCategory.SECURITY,
                    evidence = "Settings.Global.DEVELOPMENT_SETTINGS_ENABLED is set to 1.",
                    explanation = "Developer mode exposes advanced debugging options. If USB debugging is also allowed, unauthorized computers could issue shell commands when connected.",
                    limitations = "Developer options are legitimate for engineers and testers, but should be disabled when not actively needed.",
                    recommendedAction = "Toggle off Developer Options in System Settings if you are not actively debugging.",
                    systemIntentAction = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
                )
            )
        }

        if (deviceInfo.isAdbEnabled) {
            findings.add(
                AuditFinding(
                    id = "ADB_DEBUGGING_ACTIVE",
                    title = "USB Debugging (ADB) Enabled",
                    severity = Severity.HIGH,
                    category = AuditCategory.SECURITY,
                    evidence = "Settings.Global.ADB_ENABLED is active.",
                    explanation = "USB debugging allows computers connected via cable or local network to execute shell commands and install applications without standard prompt confirmation.",
                    limitations = "Detected via system global settings table.",
                    recommendedAction = "Disable USB Debugging immediately unless actively connected to an Android development environment.",
                    systemIntentAction = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
                )
            )
        }

        if (deviceInfo.rootHeuristicsDetected.isNotEmpty()) {
            findings.add(
                AuditFinding(
                    id = "ROOT_HEURISTICS_DETECTED",
                    title = "System Modification Heuristics Detected",
                    severity = Severity.CRITICAL,
                    category = AuditCategory.SECURITY,
                    evidence = deviceInfo.rootHeuristicsDetected.joinToString("; "),
                    explanation = "Indicators of superuser privileges or test-keys firmware were observed. Rooted systems bypass Android app sandboxing boundaries.",
                    limitations = deviceInfo.rootHeuristicsLimitations,
                    recommendedAction = "Investigate if this device has custom firmware installed or unlocked bootloader.",
                    systemIntentAction = "android.settings.SECURITY_SETTINGS"
                )
            )
        }

        // 2. Special Access Findings
        specialAccess.forEach { item ->
            val severity = when (item.category) {
                com.example.domain.model.SpecialAccessCategory.ACCESSIBILITY -> Severity.HIGH
                com.example.domain.model.SpecialAccessCategory.DEVICE_ADMIN -> Severity.HIGH
                com.example.domain.model.SpecialAccessCategory.VPN -> Severity.MEDIUM
                com.example.domain.model.SpecialAccessCategory.NOTIFICATION_LISTENER -> Severity.HIGH
                else -> Severity.MEDIUM
            }

            findings.add(
                AuditFinding(
                    id = "SPEC_${item.category.name}_${item.packageName}",
                    title = item.title,
                    severity = severity,
                    category = AuditCategory.SPECIAL_ACCESS,
                    affectedPackage = item.packageName,
                    affectedAppName = item.appName,
                    evidence = "Confirmed status: ${item.status} for package ${item.packageName}",
                    explanation = item.riskExplanation,
                    limitations = "Status observed through standard Android platform manager interfaces.",
                    recommendedAction = item.recommendedAction,
                    systemIntentAction = item.settingsIntentAction,
                    systemIntentPackage = item.packageName
                )
            )
        }

        // 3. Application Permission & Unknown Sources Audit
        apps.forEach { app ->
            // Check unknown installation origin
            if (app.isUnknownSource) {
                val hasHeavyPermissions = app.sensitivePermissions.count { it.isGranted } >= 2
                findings.add(
                    AuditFinding(
                        id = "UNKNOWN_SOURCE_${app.packageName}",
                        title = "Non-Store Sideloaded Application",
                        severity = if (hasHeavyPermissions) Severity.HIGH else Severity.MEDIUM,
                        category = AuditCategory.INSTALLER,
                        affectedPackage = app.packageName,
                        affectedAppName = app.appName,
                        evidence = "Installer package is: '${app.installerPackage ?: "Unknown"}'. Not distributed through Google Play Store.",
                        explanation = "Sideloaded APKs bypass Google Play Protect developer identity checks and automated malware verification.",
                        limitations = "Missing installer metadata can be caused by manual ADB install, file transfer, or alternative app stores (e.g. F-Droid). Sideloading alone does not imply malicious intent.",
                        recommendedAction = "Review this application. If unfamiliar or unexpected, uninstall it from App Info.",
                        systemIntentAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        systemIntentPackage = app.packageName
                    )
                )
            }

            // Check high-privilege combinations (Camera + Microphone + Background Location)
            val grantedSensitives = app.sensitivePermissions.filter { it.isGranted }
            val hasCam = grantedSensitives.any { it.group == "Camera" }
            val hasMic = grantedSensitives.any { it.group == "Microphone" }
            val hasBgLoc = grantedSensitives.any { it.isBackground }

            if (hasCam && hasMic && hasBgLoc && !app.isSystemApp) {
                findings.add(
                    AuditFinding(
                        id = "SURVEILLANCE_COMBO_${app.packageName}",
                        title = "Broad Sensory & Background Location Access",
                        severity = Severity.HIGH,
                        category = AuditCategory.APPLICATION,
                        affectedPackage = app.packageName,
                        affectedAppName = app.appName,
                        evidence = "Application holds active grants for Camera, Microphone, and Background Location.",
                        explanation = "This combination of permissions grants continuous sensory perception and location awareness. While valid for communication or navigation apps, it warrants review.",
                        limitations = "Permission grant confirms capability, not active or covert recording. Android 12+ displays live green indicators when sensors are actively engaged.",
                        recommendedAction = "Open App Info > Permissions and restrict background location or sensory permissions if not strictly required.",
                        systemIntentAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        systemIntentPackage = app.packageName
                    )
                )
            } else if (hasBgLoc && !app.isSystemApp) {
                findings.add(
                    AuditFinding(
                        id = "BG_LOCATION_${app.packageName}",
                        title = "Background Location Access Granted",
                        severity = Severity.MEDIUM,
                        category = AuditCategory.LOCATION,
                        affectedPackage = app.packageName,
                        affectedAppName = app.appName,
                        evidence = "ACCESS_BACKGROUND_LOCATION is actively granted.",
                        explanation = "The app can track your geographic position even while minimized or closed.",
                        limitations = "Observed from package permission flags. Does not log historical coordinate transmissions.",
                        recommendedAction = "Change location permission to 'Allow only while using the app'.",
                        systemIntentAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        systemIntentPackage = app.packageName
                    )
                )
            }

            // Check sensitive SMS reading permissions on non-system apps
            val hasSmsRead = grantedSensitives.any { it.permission == "android.permission.READ_SMS" || it.permission == "android.permission.RECEIVE_SMS" }
            if (hasSmsRead && !app.isSystemApp && !app.packageName.contains("sms", ignoreCase = true) && !app.packageName.contains("messaging", ignoreCase = true)) {
                findings.add(
                    AuditFinding(
                        id = "SMS_ACCESS_${app.packageName}",
                        title = "SMS Message Interception Capability",
                        severity = Severity.HIGH,
                        category = AuditCategory.SENSITIVE_PERMISSION,
                        affectedPackage = app.packageName,
                        affectedAppName = app.appName,
                        evidence = "Granted: READ_SMS / RECEIVE_SMS",
                        explanation = "Reading incoming SMS messages allows an app to intercept two-factor authentication codes and private text communications.",
                        limitations = "Permission verified through PackageManager.",
                        recommendedAction = "Revoke SMS permission if the app is not your designated default SMS client.",
                        systemIntentAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        systemIntentPackage = app.packageName
                    )
                )
            }

            // Stalkerware IoC and Threat Score findings
            if (app.iocMatch != null) {
                findings.add(
                    AuditFinding(
                        id = "STALKERWARE_IOC_${app.packageName}",
                        title = "Known Stalkerware Match: ${app.iocMatch.ioc.knownName}",
                        severity = Severity.CRITICAL,
                        category = AuditCategory.APPLICATION,
                        affectedPackage = app.packageName,
                        affectedAppName = app.appName,
                        evidence = app.iocMatch.matchDescription,
                        explanation = "Corresponds directly to known surveillance software cataloged by security researchers (${app.iocMatch.ioc.source}). Classification: ${app.iocMatch.ioc.classification}.",
                        limitations = "Corroborated through local offline IoC database comparison. Check if app was installed with your consent.",
                        recommendedAction = "Review immediately. If unexpected, follow safe removal procedures in Stalkerware Audit.",
                        systemIntentAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        systemIntentPackage = app.packageName
                    )
                )
            } else if (app.threatScore >= 60) {
                findings.add(
                    AuditFinding(
                        id = "STALKERWARE_ELEVATED_${app.packageName}",
                        title = "High Stalkerware Threat Score (${app.threatScore}/100)",
                        severity = Severity.HIGH,
                        category = AuditCategory.APPLICATION,
                        affectedPackage = app.packageName,
                        affectedAppName = app.appName,
                        evidence = "Threat Score: ${app.threatScore}/100. Indicators: " + app.contributingThreatIndicators.joinToString("; ") { "${it.title} (+${it.score})" },
                        explanation = ThreatScoringEngine.WARNING_TEXT,
                        limitations = "Calculated by deterministic ThreatScoringEngine. Indicates elevated surveillance risk signals, not verified malicious intent.",
                        recommendedAction = "Inspect application origin, accessibility status, and device administrator privileges.",
                        systemIntentAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        systemIntentPackage = app.packageName
                    )
                )
            }
        }

        // Add Informational summary finding for platform limitations
        findings.add(
            AuditFinding(
                id = "PLATFORM_LIMITS_DISCLOSURE",
                title = "Android Platform Transparency & Boundaries",
                severity = Severity.INFORMATIONAL,
                category = AuditCategory.DEVICE,
                evidence = "Audit conducted using public Android SDK APIs (Build, PackageManager, AppOpsManager).",
                explanation = "AB.M. Scan operates strictly within Android's legitimate sandbox. The OS does not expose historical sensor timelines or private data contents to third-party audit apps.",
                limitations = "Historical camera/microphone access timestamps are managed exclusively by Android's Privacy Dashboard in System Settings.",
                recommendedAction = "Use Android's built-in Privacy Dashboard for real-time and 24-hour sensor access logs.",
                systemIntentAction = "android.settings.PRIVACY_SETTINGS"
            )
        )

        return findings.sortedByDescending { it.severity.weight }
    }

    fun calculateRiskScore(findings: List<AuditFinding>): Pair<Int, String> {
        var score = 100
        var criticalCount = 0
        var highCount = 0
        var mediumCount = 0
        var lowCount = 0

        findings.forEach { finding ->
            when (finding.severity) {
                Severity.CRITICAL -> {
                    score -= 25
                    criticalCount++
                }
                Severity.HIGH -> {
                    score -= 12
                    highCount++
                }
                Severity.MEDIUM -> {
                    score -= 6
                    mediumCount++
                }
                Severity.LOW -> {
                    score -= 2
                    lowCount++
                }
                Severity.INFORMATIONAL -> {
                    // No score deduction for educational/transparency notices
                }
            }
        }

        val clampedScore = score.coerceIn(15, 100)

        val summary = when {
            clampedScore >= 90 -> "Strong Security Posture. Minimal elevated privileges or high-risk findings detected."
            clampedScore >= 75 -> "Good Security Posture. A few sensitive permission grants or sideloaded apps require review."
            clampedScore >= 55 -> "Moderate Concern. Multiple high-privilege permissions, debug settings, or unknown origins observed."
            else -> "Attention Recommended. Significant security or privacy concerns identified (e.g. no screen lock or critical modifications)."
        }

        return Pair(clampedScore, summary)
    }
}
