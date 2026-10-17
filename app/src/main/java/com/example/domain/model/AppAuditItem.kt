package com.example.domain.model

data class AppAuditItem(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val installerPackage: String?,
    val installerLabel: String,
    val isUnknownSource: Boolean,
    val declaredPermissions: List<String>,
    val grantedPermissions: List<String>,
    val sensitivePermissions: List<SensitivePermissionItem>,
    val specialAccessFlags: List<String>,
    val maxSeverity: Severity = Severity.INFORMATIONAL,
    val findingsCount: Int = 0,
    val hasLauncherActivity: Boolean = true,
    val signingCertSha256: String? = null,
    val initiatingPackage: String? = null,
    val originatingPackage: String? = null,
    val threatScore: Int = 0,
    val threatCategory: ThreatCategory = ThreatCategory.LOW,
    val contributingThreatIndicators: List<ThreatIndicator> = emptyList(),
    val iocMatch: IocMatchResult? = null
)

data class DeviceSecurityInfo(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val device: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val isDeviceSecure: Boolean,
    val isDeveloperOptionsEnabled: Boolean,
    val isAdbEnabled: Boolean,
    val rootHeuristicsDetected: List<String>,
    val rootHeuristicsLimitations: String,
    val isDeviceEncrypted: Boolean
)

data class ScanReport(
    val scanId: String,
    val timestamp: Long,
    val deviceInfo: DeviceSecurityInfo,
    val totalAppsScanned: Int,
    val systemAppsCount: Int,
    val userAppsCount: Int,
    val apps: List<AppAuditItem>,
    val findings: List<AuditFinding>,
    val findingsBySeverity: Map<Severity, Int>,
    val completedStages: List<ScanStage>,
    val unsupportedChecks: List<String>,
    val overallRiskScore: Int,
    val overallRiskSummary: String,
    val stalkerwareHighRiskCount: Int = 0,
    val stalkerwareIocMatchCount: Int = 0
)
