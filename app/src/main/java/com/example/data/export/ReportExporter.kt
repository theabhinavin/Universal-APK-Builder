package com.example.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.domain.model.ScanReport
import com.example.domain.model.Severity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault())

    fun toJsonString(report: ScanReport): String {
        val root = JSONObject()
        root.put("report_title", "AB.M. Scan — Privacy Audit Report")
        root.put("brand_signature", "ABM | Abhinav Mishra")
        root.put("scan_id", report.scanId)
        root.put("timestamp", report.timestamp)
        root.put("formatted_date", dateFormat.format(Date(report.timestamp)))

        // Device
        val dev = JSONObject()
        dev.put("manufacturer", report.deviceInfo.manufacturer)
        dev.put("model", report.deviceInfo.model)
        dev.put("android_version", report.deviceInfo.androidVersion)
        dev.put("api_level", report.deviceInfo.apiLevel)
        dev.put("security_patch", report.deviceInfo.securityPatch)
        dev.put("is_device_secure", report.deviceInfo.isDeviceSecure)
        dev.put("is_developer_options_enabled", report.deviceInfo.isDeveloperOptionsEnabled)
        dev.put("is_adb_enabled", report.deviceInfo.isAdbEnabled)
        dev.put("is_encrypted", report.deviceInfo.isDeviceEncrypted)
        val roots = JSONArray()
        report.deviceInfo.rootHeuristicsDetected.forEach { roots.put(it) }
        dev.put("root_heuristics", roots)
        root.put("device_info", dev)

        // Metrics
        val metrics = JSONObject()
        metrics.put("total_apps_scanned", report.totalAppsScanned)
        metrics.put("system_apps", report.systemAppsCount)
        metrics.put("user_apps", report.userAppsCount)
        metrics.put("risk_score", report.overallRiskScore)
        metrics.put("risk_summary", report.overallRiskSummary)
        val sevJson = JSONObject()
        report.findingsBySeverity.forEach { (sev, count) ->
            sevJson.put(sev.name, count)
        }
        metrics.put("findings_by_severity", sevJson)
        metrics.put("stalkerware_high_risk_count", report.stalkerwareHighRiskCount)
        metrics.put("stalkerware_ioc_match_count", report.stalkerwareIocMatchCount)
        root.put("audit_metrics", metrics)

        // Findings
        val findingsArray = JSONArray()
        report.findings.forEach { finding ->
            val f = JSONObject()
            f.put("id", finding.id)
            f.put("title", finding.title)
            f.put("severity", finding.severity.name)
            f.put("category", finding.category.name)
            f.put("affected_package", finding.affectedPackage ?: "")
            f.put("affected_app_name", finding.affectedAppName ?: "")
            f.put("evidence", finding.evidence)
            f.put("explanation", finding.explanation)
            f.put("limitations", finding.limitations)
            f.put("recommended_action", finding.recommendedAction)
            findingsArray.put(f)
        }
        root.put("findings", findingsArray)

        // Apps summary (high-level)
        val appsArray = JSONArray()
        report.apps.take(50).forEach { app ->
            val a = JSONObject()
            a.put("package_name", app.packageName)
            a.put("app_name", app.appName)
            a.put("version", app.versionName)
            a.put("is_system", app.isSystemApp)
            a.put("installer", app.installerLabel)
            a.put("is_unknown_source", app.isUnknownSource)
            a.put("has_launcher_activity", app.hasLauncherActivity)
            a.put("threat_score", app.threatScore)
            a.put("threat_category", app.threatCategory.name)
            if (app.signingCertSha256 != null) {
                a.put("cert_sha256", app.signingCertSha256)
            }
            if (app.iocMatch != null) {
                val iocObj = JSONObject()
                iocObj.put("known_name", app.iocMatch.ioc.knownName)
                iocObj.put("is_corroborated", app.iocMatch.isCertCorroborated)
                iocObj.put("classification", app.iocMatch.ioc.classification)
                a.put("ioc_match", iocObj)
            }
            a.put("sensitive_permissions_granted", app.sensitivePermissions.count { it.isGranted })
            appsArray.put(a)
        }
        root.put("applications_sample", appsArray)

        return root.toString(2)
    }

    fun toReadableSummary(report: ScanReport): String {
        val dateStr = dateFormat.format(Date(report.timestamp))
        val sb = StringBuilder()
        sb.appendLine("=========================================")
        sb.appendLine("       AB.M. SCAN — PRIVACY AUDIT        ")
        sb.appendLine("        ABM | Abhinav Mishra            ")
        sb.appendLine("=========================================")
        sb.appendLine("Scan Date       : $dateStr")
        sb.appendLine("Device Model    : ${report.deviceInfo.manufacturer} ${report.deviceInfo.model}")
        sb.appendLine("Android OS      : Android ${report.deviceInfo.androidVersion} (API ${report.deviceInfo.apiLevel})")
        sb.appendLine("Security Patch  : ${report.deviceInfo.securityPatch}")
        sb.appendLine("Screen Lock     : ${if (report.deviceInfo.isDeviceSecure) "Protected" else "UNPROTECTED"}")
        sb.appendLine("Security Score  : ${report.overallRiskScore}/100")
        sb.appendLine("Status Summary  : ${report.overallRiskSummary}")
        sb.appendLine("-----------------------------------------")
        sb.appendLine("Apps Inspected  : ${report.totalAppsScanned} (${report.userAppsCount} user, ${report.systemAppsCount} system)")
        sb.appendLine("Surveillance    : ${report.stalkerwareHighRiskCount} high-threat app(s), ${report.stalkerwareIocMatchCount} IoC match(es)")
        sb.appendLine("Critical Issues : ${report.findingsBySeverity[Severity.CRITICAL] ?: 0}")
        sb.appendLine("High Concerns   : ${report.findingsBySeverity[Severity.HIGH] ?: 0}")
        sb.appendLine("Medium Warnings : ${report.findingsBySeverity[Severity.MEDIUM] ?: 0}")
        sb.appendLine("Low Findings    : ${report.findingsBySeverity[Severity.LOW] ?: 0}")
        sb.appendLine("Informational   : ${report.findingsBySeverity[Severity.INFORMATIONAL] ?: 0}")
        sb.appendLine("=========================================")
        if (report.stalkerwareHighRiskCount > 0 || report.stalkerwareIocMatchCount > 0) {
            sb.appendLine("\nSTALKERWARE & SURVEILLANCE WARNING:")
            sb.appendLine("Potential stalkerware — multiple high-risk indicators or known IoC matches detected.")
            sb.appendLine("Safety note: If under surveillance, seek support on an unmonitored device.")
            sb.appendLine("=========================================")
        }
        sb.appendLine("\nTOP AUDIT FINDINGS:")
        val topFindings = report.findings.filter { it.severity != Severity.INFORMATIONAL }.take(8)
        if (topFindings.isEmpty()) {
            sb.appendLine("No severe privacy or security concerns identified.")
        } else {
            topFindings.forEachIndexed { i, f ->
                sb.appendLine("${i + 1}. [${f.severity.label.uppercase()}] ${f.title}")
                if (!f.affectedAppName.isNullOrEmpty()) {
                    sb.appendLine("   Target   : ${f.affectedAppName} (${f.affectedPackage})")
                }
                sb.appendLine("   Evidence : ${f.evidence}")
                sb.appendLine("   Action   : ${f.recommendedAction}")
                sb.appendLine()
            }
        }
        sb.appendLine("=========================================")
        sb.appendLine("Audit executed locally by AB.M. Scan.")
        sb.appendLine("Brand signature: ABM — Abhinav Mishra")
        sb.appendLine("=========================================")
        return sb.toString()
    }

    fun shareReport(context: Context, report: ScanReport) {
        val summaryText = toReadableSummary(report)
        val jsonText = toJsonString(report)
        val fileName = "ABM_Scan_Report_${fileDateFormat.format(Date(report.timestamp))}.json"

        try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()
            val file = File(reportsDir, fileName)
            FileOutputStream(file).use { out ->
                out.write(jsonText.toByteArray(Charsets.UTF_8))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, "AB.M. Scan — Privacy Audit Report")
                putExtra(Intent.EXTRA_TEXT, summaryText)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share AB.M. Scan Audit Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Fallback to plain text share if FileProvider fails
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "AB.M. Scan — Privacy Audit Report")
                putExtra(Intent.EXTRA_TEXT, summaryText + "\n\n--- JSON DATA ---\n" + jsonText)
            }
            val chooser = Intent.createChooser(shareIntent, "Share AB.M. Scan Audit Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}
