package com.example

import com.example.domain.model.AppAuditItem
import com.example.domain.model.SensitivePermissionItem
import com.example.domain.model.Severity
import com.example.domain.model.ThreatCategory
import com.example.scanner.PermissionAnalyzer
import com.example.scanner.StalkerwareIocDatabase
import com.example.scanner.ThreatScoringEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `sensitive permissions classification identifies camera and location`() {
        assertTrue(PermissionAnalyzer.isSensitivePermission("android.permission.CAMERA"))
        assertTrue(PermissionAnalyzer.isSensitivePermission("android.permission.RECORD_AUDIO"))
        assertTrue(PermissionAnalyzer.isSensitivePermission("android.permission.ACCESS_FINE_LOCATION"))
        assertTrue(PermissionAnalyzer.isSensitivePermission("android.permission.ACCESS_BACKGROUND_LOCATION"))
        assertTrue(PermissionAnalyzer.isSensitivePermission("android.permission.READ_SMS"))
        assertTrue(PermissionAnalyzer.isSensitivePermission("android.permission.READ_CONTACTS"))
        assertTrue(PermissionAnalyzer.isSensitivePermission("android.permission.BLUETOOTH_SCAN"))
        assertTrue(PermissionAnalyzer.isSensitivePermission("android.permission.POST_NOTIFICATIONS"))

        assertFalse(PermissionAnalyzer.isSensitivePermission("android.permission.INTERNET"))
        assertFalse(PermissionAnalyzer.isSensitivePermission("android.permission.ACCESS_NETWORK_STATE"))
        assertFalse(PermissionAnalyzer.isSensitivePermission("android.permission.VIBRATE"))
    }

    @Test
    fun `threat scoring engine accurately calculates individual rules`() {
        val engine = ThreatScoringEngine()

        // 1. Accessibility service only (+40)
        val app1 = createTestApp(isUnknown = false, hasLauncher = true)
        val res1 = engine.evaluate(app1, isAccessibilityServiceActive = true, isDeviceAdminActive = false)
        assertEquals(40, res1.score)
        assertEquals(ThreatCategory.ELEVATED_RISK, res1.category)
        assertFalse(res1.requiresProminentWarning) // requires >= 60

        // 2. Device admin only (+30)
        val res2 = engine.evaluate(app1, isAccessibilityServiceActive = false, isDeviceAdminActive = true)
        assertEquals(30, res2.score)
        assertEquals(ThreatCategory.REVIEW_RECOMMENDED, res2.category)

        // 3. Unrecognized installer only (+15)
        val appUnknown = createTestApp(isUnknown = true, hasLauncher = true)
        val res3 = engine.evaluate(appUnknown, isAccessibilityServiceActive = false, isDeviceAdminActive = false)
        assertEquals(15, res3.score)
        assertEquals(ThreatCategory.LOW, res3.category)

        // 4. No launcher activity only (+25)
        val appNoLauncher = createTestApp(isUnknown = false, hasLauncher = false)
        val res4 = engine.evaluate(appNoLauncher, isAccessibilityServiceActive = false, isDeviceAdminActive = false)
        assertEquals(25, res4.score)
        assertEquals(ThreatCategory.REVIEW_RECOMMENDED, res4.category)

        // 5. Surveillance sensor combination (Background Location + Mic + Internet) (+25)
        val appSurveillance = createTestApp(
            isUnknown = false,
            hasLauncher = true,
            declaredPerms = listOf("android.permission.INTERNET"),
            sensitives = listOf(
                SensitivePermissionItem("android.permission.RECORD_AUDIO", "Microphone", isGranted = true, description = "Mic"),
                SensitivePermissionItem("android.permission.ACCESS_BACKGROUND_LOCATION", "Location", isGranted = true, isBackground = true, description = "Bg Loc")
            )
        )
        val res5 = engine.evaluate(appSurveillance, isAccessibilityServiceActive = false, isDeviceAdminActive = false)
        assertEquals(25, res5.score)
    }

    @Test
    fun `threat scoring engine clamps combined scores to 100 and flags warnings`() {
        val engine = ThreatScoringEngine()

        // Combine all indicators: 40 (Accessibility) + 30 (Admin) + 15 (Unknown) + 25 (No launcher) + 25 (Surveillance) = 135
        val highThreatApp = createTestApp(
            isUnknown = true,
            hasLauncher = false,
            declaredPerms = listOf("android.permission.INTERNET"),
            sensitives = listOf(
                SensitivePermissionItem("android.permission.RECORD_AUDIO", "Microphone", isGranted = true, description = "Mic"),
                SensitivePermissionItem("android.permission.ACCESS_BACKGROUND_LOCATION", "Location", isGranted = true, isBackground = true, description = "Bg Loc")
            )
        )

        val result = engine.evaluate(highThreatApp, isAccessibilityServiceActive = true, isDeviceAdminActive = true)
        assertEquals(100, result.score) // Clamped to 100
        assertEquals(ThreatCategory.VERY_HIGH_RISK, result.category)
        assertTrue(result.requiresProminentWarning) // >= 60 triggers warning
        assertEquals(5, result.indicators.size)
    }

    @Test
    fun `system apps are exempt from third-party stalkerware scoring`() {
        val engine = ThreatScoringEngine()
        val systemApp = createTestApp(isSystem = true, isUnknown = false, hasLauncher = false)
        val result = engine.evaluate(systemApp, isAccessibilityServiceActive = true, isDeviceAdminActive = true)
        assertEquals(0, result.score)
        assertEquals(ThreatCategory.LOW, result.category)
        assertFalse(result.requiresProminentWarning)
    }

    @Test
    fun `ioc database matches package and corroborates certificate fingerprints`() {
        // 1. Exact match with corroborated SHA-256
        val matchCorroborated = StalkerwareIocDatabase.findMatch(
            packageName = "com.mspy.android",
            certSha256 = "6F:73:9A:82:5E:4C:9D:61:74:02:44:E2:B0:81:49:15:37:92:A9:39:DF:44:A2:72:08:C1:28:FE:8B:13:95:C7"
        )
        assertNotNull(matchCorroborated)
        assertEquals("mSpy Mobile Tracker", matchCorroborated?.ioc?.knownName)
        assertTrue(matchCorroborated!!.isCertCorroborated)

        // 2. Package-only match without certificate
        val matchPackageOnly = StalkerwareIocDatabase.findMatch(
            packageName = "com.trackview",
            certSha256 = null
        )
        assertNotNull(matchPackageOnly)
        assertEquals("TrackView Spy Tool", matchPackageOnly?.ioc?.knownName)
        assertFalse(matchPackageOnly!!.isCertCorroborated)

        // 3. Unmatched package
        val noMatch = StalkerwareIocDatabase.findMatch(
            packageName = "com.example.safeapp",
            certSha256 = "AA:BB:CC"
        )
        assertNull(noMatch)
    }

    private fun createTestApp(
        packageName: String = "com.test.app",
        isSystem: Boolean = false,
        isUnknown: Boolean = false,
        hasLauncher: Boolean = true,
        declaredPerms: List<String> = emptyList(),
        sensitives: List<SensitivePermissionItem> = emptyList()
    ): AppAuditItem {
        return AppAuditItem(
            packageName = packageName,
            appName = "Test App",
            versionName = "1.0",
            versionCode = 1,
            isSystemApp = isSystem,
            installerPackage = if (isUnknown) null else "com.android.vending",
            installerLabel = if (isUnknown) "Unknown / Sideloaded" else "Google Play Store",
            isUnknownSource = isUnknown,
            declaredPermissions = declaredPerms,
            grantedPermissions = emptyList(),
            sensitivePermissions = sensitives,
            specialAccessFlags = emptyList(),
            maxSeverity = Severity.INFORMATIONAL,
            hasLauncherActivity = hasLauncher
        )
    }
}
