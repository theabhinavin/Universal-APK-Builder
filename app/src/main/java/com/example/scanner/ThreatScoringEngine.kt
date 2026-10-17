package com.example.scanner

import com.example.domain.model.AppAuditItem
import com.example.domain.model.ThreatCategory
import com.example.domain.model.ThreatIndicator

class ThreatScoringEngine {

    data class EvaluationResult(
        val score: Int,
        val category: ThreatCategory,
        val indicators: List<ThreatIndicator>,
        val requiresProminentWarning: Boolean
    )

    fun evaluate(
        app: AppAuditItem,
        isAccessibilityServiceActive: Boolean,
        isDeviceAdminActive: Boolean
    ): EvaluationResult {
        // System applications are generally exempt from third-party stalkerware scoring unless modified
        if (app.isSystemApp) {
            return EvaluationResult(
                score = 0,
                category = ThreatCategory.LOW,
                indicators = emptyList(),
                requiresProminentWarning = false
            )
        }

        val indicators = mutableListOf<ThreatIndicator>()

        // Rule 1: Unexpected third-party accessibility service enabled (+40)
        if (isAccessibilityServiceActive) {
            indicators.add(
                ThreatIndicator(
                    id = "THREAT_ACCESSIBILITY_ENABLED",
                    title = "Active Accessibility Service",
                    score = 40,
                    evidence = "App has an active accessibility service registered in system settings.",
                    explanation = "Accessibility access enables reading screen content across all applications and intercepting keystrokes. Often leveraged by stalkerware to log chats."
                )
            )
        }

        // Rule 2: Unexpected third-party device administrator active (+30)
        if (isDeviceAdminActive) {
            indicators.add(
                ThreatIndicator(
                    id = "THREAT_DEVICE_ADMIN_ACTIVE",
                    title = "Active Device Administrator",
                    score = 30,
                    evidence = "App holds active Device Administrator privileges.",
                    explanation = "Device administrator privileges prevent normal uninstallation and allow remote wiping or lock enforcement."
                )
            )
        }

        // Rule 3: Unrecognized or suspicious installation source (+15)
        if (app.isUnknownSource) {
            indicators.add(
                ThreatIndicator(
                    id = "THREAT_UNKNOWN_INSTALLER",
                    title = "Unrecognized Installation Source",
                    score = 15,
                    evidence = "Installer package is '${app.installerPackage ?: "None / Sideloaded"}'.",
                    explanation = "App was not installed through Google Play Store or OEM store. Commercial stalkerware is almost exclusively sideloaded."
                )
            )
        }

        // Rule 4: Third-party application with no launcher entry (+25)
        if (!app.hasLauncherActivity) {
            indicators.add(
                ThreatIndicator(
                    id = "THREAT_NO_LAUNCHER_ENTRY",
                    title = "No Launcher Icon (Hidden App)",
                    score = 25,
                    evidence = "Application does not expose a launchable MAIN activity with CATEGORY_LAUNCHER.",
                    explanation = "Absence of a home screen icon allows the application to operate covertly without drawing user visual attention."
                )
            )
        }

        // Rule 5: High-risk combination of background location + microphone + Internet (+25)
        val hasInternet = app.declaredPermissions.contains("android.permission.INTERNET")
        val hasMicGranted = app.sensitivePermissions.any { it.group == "Microphone" && it.isGranted }
        val hasBgLocationGranted = app.sensitivePermissions.any { it.isBackground && it.isGranted }

        if (hasInternet && hasMicGranted && hasBgLocationGranted) {
            indicators.add(
                ThreatIndicator(
                    id = "THREAT_SURVEILLANCE_COMBO",
                    title = "Surveillance Sensor Combination",
                    score = 25,
                    evidence = "App actively holds Background Location and Microphone permissions, alongside Internet access.",
                    explanation = "This capability allows tracking geographic movement and capturing ambient sound while running in the background and transmitting data remotely."
                )
            )
        }

        // Calculate aggregated score
        val rawScore = indicators.sumOf { it.score }
        val clampedScore = rawScore.coerceIn(0, 100)
        val category = ThreatCategory.fromScore(clampedScore)
        val requiresWarning = clampedScore >= 60

        return EvaluationResult(
            score = clampedScore,
            category = category,
            indicators = indicators,
            requiresProminentWarning = requiresWarning
        )
    }

    companion object {
        const val WARNING_TEXT = "Potential stalkerware — multiple high-risk indicators detected. Manual investigation recommended."
    }
}
