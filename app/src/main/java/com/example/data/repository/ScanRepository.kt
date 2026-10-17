package com.example.data.repository

import android.content.Context
import com.example.data.export.ReportExporter
import com.example.data.local.AppDatabase
import com.example.data.local.LatestScanEntity
import com.example.domain.model.AppAuditItem
import com.example.domain.model.DeviceSecurityInfo
import com.example.domain.model.ScanReport
import com.example.domain.model.ScanStage
import com.example.domain.model.Severity
import com.example.domain.model.SpecialAccessItem
import com.example.scanner.AppScanner
import com.example.scanner.DeviceScanner
import com.example.scanner.FindingEngine
import com.example.scanner.SpecialAccessScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class ScanRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.latestScanDao()

    private val deviceScanner = DeviceScanner(context)
    private val appScanner = AppScanner(context)
    private val specialAccessScanner = SpecialAccessScanner(context)
    private val findingEngine = FindingEngine(context)

    fun getLatestScanEntity(): Flow<LatestScanEntity?> = dao.getLatestScan()

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            dao.clearScan()
        }
    }

    suspend fun executeScan(
        onProgress: (stage: ScanStage, progressPercent: Float) -> Unit
    ): ScanReport = withContext(Dispatchers.IO) {
        val completedStages = mutableListOf<ScanStage>()

        // Stage 1: Device Configuration
        onProgress(ScanStage.DEVICE_CONFIG, 0.10f)
        val deviceInfo: DeviceSecurityInfo = deviceScanner.scanDevice()
        completedStages.add(ScanStage.DEVICE_CONFIG)
        delay(120) // Smooth visual transition between genuine inspections

        // Stage 2: Installed Apps Inventory
        onProgress(ScanStage.APP_INVENTORY, 0.25f)
        val apps: List<AppAuditItem> = appScanner.scanInstalledApps()
        completedStages.add(ScanStage.APP_INVENTORY)
        delay(120)

        // Stage 3: Permission Review
        onProgress(ScanStage.PERMISSION_REVIEW, 0.40f)
        // Permissions already analyzed per app
        completedStages.add(ScanStage.PERMISSION_REVIEW)
        delay(100)

        // Stage 4: Camera Privacy
        onProgress(ScanStage.CAMERA_PRIVACY, 0.50f)
        completedStages.add(ScanStage.CAMERA_PRIVACY)
        delay(80)

        // Stage 5: Microphone Privacy
        onProgress(ScanStage.MICROPHONE_PRIVACY, 0.60f)
        completedStages.add(ScanStage.MICROPHONE_PRIVACY)
        delay(80)

        // Stage 6: Location Privacy
        onProgress(ScanStage.LOCATION_PRIVACY, 0.70f)
        completedStages.add(ScanStage.LOCATION_PRIVACY)
        delay(80)

        // Stage 7: Special Access
        onProgress(ScanStage.SPECIAL_ACCESS, 0.80f)
        val specialAccess: List<SpecialAccessItem> = specialAccessScanner.scanSpecialAccess()
        completedStages.add(ScanStage.SPECIAL_ACCESS)
        delay(100)

        // Stage 8: Unknown Sources
        onProgress(ScanStage.UNKNOWN_SOURCES, 0.90f)
        completedStages.add(ScanStage.UNKNOWN_SOURCES)
        delay(80)

        // Stage 9: Security Posture & Findings Generation
        onProgress(ScanStage.SECURITY_POSTURE, 0.95f)
        val findings = findingEngine.generateFindings(deviceInfo, apps, specialAccess)
        val (score, summary) = findingEngine.calculateRiskScore(findings)
        completedStages.add(ScanStage.SECURITY_POSTURE)
        onProgress(ScanStage.SECURITY_POSTURE, 1.0f)

        val severityCounts = Severity.values().associateWith { sev ->
            findings.count { it.severity == sev }
        }

        val report = ScanReport(
            scanId = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            deviceInfo = deviceInfo,
            totalAppsScanned = apps.size,
            systemAppsCount = apps.count { it.isSystemApp },
            userAppsCount = apps.count { !it.isSystemApp },
            apps = apps,
            findings = findings,
            findingsBySeverity = severityCounts,
            completedStages = completedStages,
            unsupportedChecks = listOf(
                "Historical 24-hour sensor timeline is restricted by Android OS to system Privacy Dashboard",
                "Kernel memory integrity and physical hardware tampering cannot be inspected by unprivileged apps"
            ),
            overallRiskScore = score,
            overallRiskSummary = summary,
            stalkerwareHighRiskCount = apps.count { it.threatScore >= 60 },
            stalkerwareIocMatchCount = apps.count { it.iocMatch != null }
        )

        // Persist single latest scan to Room
        val reportJson = ReportExporter.toJsonString(report)
        dao.saveLatestScan(
            LatestScanEntity(
                id = 1,
                timestamp = report.timestamp,
                totalApps = report.totalAppsScanned,
                criticalCount = severityCounts[Severity.CRITICAL] ?: 0,
                highCount = severityCounts[Severity.HIGH] ?: 0,
                mediumCount = severityCounts[Severity.MEDIUM] ?: 0,
                lowCount = severityCounts[Severity.LOW] ?: 0,
                riskScore = score,
                riskSummary = summary,
                reportJson = reportJson
            )
        )

        report
    }
}
