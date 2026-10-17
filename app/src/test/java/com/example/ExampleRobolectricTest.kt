package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.AuditCategory
import com.example.domain.model.AuditFinding
import com.example.domain.model.DeviceSecurityInfo
import com.example.domain.model.ScanReport
import com.example.domain.model.ScanStage
import com.example.domain.model.Severity
import com.example.data.export.ReportExporter
import com.example.scanner.FindingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches AB_M_Scan`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AB.M. Scan", appName)
    }

    @Test
    fun `finding engine calculates risk score correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = FindingEngine(context)

        val sampleFindings = listOf(
            AuditFinding(
                id = "TEST_CRIT",
                title = "Critical SU Found",
                severity = Severity.CRITICAL,
                category = AuditCategory.SECURITY,
                evidence = "su in /system/bin",
                explanation = "Root binary",
                limitations = "Heuristic check",
                recommendedAction = "Inspect firmware"
            ),
            AuditFinding(
                id = "TEST_HIGH",
                title = "High Risk Perm",
                severity = Severity.HIGH,
                category = AuditCategory.APPLICATION,
                evidence = "Camera + Mic + Location",
                explanation = "Sensor combo",
                limitations = "API check",
                recommendedAction = "Review in Settings"
            )
        )

        val (score, summary) = engine.calculateRiskScore(sampleFindings)
        // 100 - 25 (critical) - 12 (high) = 63
        assertEquals(63, score)
        assertTrue(summary.isNotEmpty())
    }

    @Test
    fun `report exporter formats json and summary with ABM branding`() {
        val deviceInfo = DeviceSecurityInfo(
            manufacturer = "Google",
            model = "Pixel 8",
            brand = "google",
            device = "shiba",
            androidVersion = "14",
            apiLevel = 34,
            securityPatch = "2024-05-01",
            isDeviceSecure = true,
            isDeveloperOptionsEnabled = false,
            isAdbEnabled = false,
            rootHeuristicsDetected = emptyList(),
            rootHeuristicsLimitations = "Heuristic limits",
            isDeviceEncrypted = true
        )

        val report = ScanReport(
            scanId = "test-uuid-1234",
            timestamp = 1715000000000L,
            deviceInfo = deviceInfo,
            totalAppsScanned = 42,
            systemAppsCount = 30,
            userAppsCount = 12,
            apps = emptyList(),
            findings = listOf(
                AuditFinding(
                    id = "TEST_INFO",
                    title = "Transparency Disclosure",
                    severity = Severity.INFORMATIONAL,
                    category = AuditCategory.DEVICE,
                    evidence = "Public APIs",
                    explanation = "Standard sandbox",
                    limitations = "None",
                    recommendedAction = "None"
                )
            ),
            findingsBySeverity = mapOf(Severity.INFORMATIONAL to 1),
            completedStages = ScanStage.values().toList(),
            unsupportedChecks = emptyList(),
            overallRiskScore = 100,
            overallRiskSummary = "Optimal Security Posture",
            stalkerwareHighRiskCount = 1,
            stalkerwareIocMatchCount = 1
        )

        val json = ReportExporter.toJsonString(report)
        assertTrue("JSON must contain brand signature", json.contains("ABM | Abhinav Mishra"))
        assertTrue("JSON must contain report title", json.contains("AB.M. Scan — Privacy Audit Report"))
        assertTrue("JSON must contain stalkerware metrics", json.contains("stalkerware_high_risk_count"))
        assertTrue("JSON must contain Pixel 8", json.contains("Pixel 8"))

        val summary = ReportExporter.toReadableSummary(report)
        assertTrue("Summary must contain AB.M. Scan header", summary.contains("AB.M. SCAN — PRIVACY AUDIT"))
        assertTrue("Summary must contain brand owner", summary.contains("Abhinav Mishra"))
        assertTrue("Summary must contain surveillance section", summary.contains("STALKERWARE & SURVEILLANCE WARNING"))
    }

    @Test
    fun `stalkerware ioc database correctly identifies known package`() {
        val match = com.example.scanner.StalkerwareIocDatabase.findMatch("com.mspy.android", null)
        assertNotNull(match)
        assertEquals("mSpy Mobile Tracker", match?.ioc?.knownName)
        assertEquals("Commercial Stalkerware", match?.ioc?.classification)
    }

    @Test
    fun `threat scoring engine evaluates high risk app indicators`() {
        val engine = com.example.scanner.ThreatScoringEngine()
        val dummyApp = com.example.domain.model.AppAuditItem(
            packageName = "com.suspicious.stealthapp",
            appName = "System Battery Helper",
            versionName = "1.0",
            versionCode = 1,
            isSystemApp = false,
            installerPackage = null,
            installerLabel = "Sideloaded / Direct Install",
            isUnknownSource = true,
            declaredPermissions = listOf(
                "android.permission.INTERNET",
                "android.permission.RECORD_AUDIO",
                "android.permission.ACCESS_BACKGROUND_LOCATION"
            ),
            grantedPermissions = listOf(
                "android.permission.INTERNET",
                "android.permission.RECORD_AUDIO",
                "android.permission.ACCESS_BACKGROUND_LOCATION"
            ),
            sensitivePermissions = listOf(
                com.example.domain.model.SensitivePermissionItem(
                    permission = "android.permission.RECORD_AUDIO",
                    group = "Microphone",
                    isGranted = true,
                    isBackground = false,
                    description = "Captures audio"
                ),
                com.example.domain.model.SensitivePermissionItem(
                    permission = "android.permission.ACCESS_BACKGROUND_LOCATION",
                    group = "Location",
                    isGranted = true,
                    isBackground = true,
                    description = "Tracks location in background"
                )
            ),
            specialAccessFlags = emptyList(),
            maxSeverity = Severity.HIGH,
            findingsCount = 3,
            hasLauncherActivity = false,
            signingCertSha256 = null
        )

        val eval = engine.evaluate(
            app = dummyApp,
            isAccessibilityServiceActive = true,
            isDeviceAdminActive = false
        )

        // Accessibility (40) + Unknown installer (15) + No launcher (25) + Surveillance combo (25) = 100
        assertTrue("Score should be >= 60 for high risk", eval.score >= 60)
        assertEquals(com.example.domain.model.ThreatCategory.VERY_HIGH_RISK, eval.category)
        assertTrue(eval.requiresProminentWarning)
        assertTrue(eval.indicators.any { it.id == "THREAT_ACCESSIBILITY_ENABLED" })
        assertTrue(eval.indicators.any { it.id == "THREAT_NO_LAUNCHER_ENTRY" })
    }
}
