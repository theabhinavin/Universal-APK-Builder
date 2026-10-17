package com.example.scanner

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.view.accessibility.AccessibilityManager
import com.example.domain.model.AppAuditItem
import com.example.domain.model.Severity
import java.security.MessageDigest

class AppScanner(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val scoringEngine = ThreatScoringEngine()

    fun scanInstalledApps(): List<AppAuditItem> {
        val packages: List<PackageInfo> = try {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNATURES
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(flags)
            }
        } catch (_: Exception) {
            emptyList()
        }

        // Active accessibility packages
        val activeAccessibilityPackages = getActiveAccessibilityPackages()

        // Active device administrator packages
        val activeDeviceAdminPackages = getActiveDeviceAdminPackages()

        return packages.mapNotNull { pkg ->
            val appInfo = pkg.applicationInfo ?: return@mapNotNull null
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val appLabel = try {
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg.packageName
            }

            // Install source details
            val (installerPackage, initiatingPackage, originatingPackage) = getInstallSourceDetails(pkg.packageName)
            val installerLabel = resolveInstallerLabel(installerPackage, isSystem)
            val isUnknownSource = !isSystem && (installerPackage == null || !KNOWN_SAFE_INSTALLERS.contains(installerPackage))

            // Launcher activity check
            val hasLauncher = try {
                packageManager.getLaunchIntentForPackage(pkg.packageName) != null
            } catch (_: Exception) {
                false
            }

            // Certificate SHA-256 fingerprint
            val certSha256 = extractCertSha256(pkg)

            // Permissions
            val declared = pkg.requestedPermissions?.toList() ?: emptyList()
            val flags = pkg.requestedPermissionsFlags ?: IntArray(declared.size)
            val granted = mutableListOf<String>()
            for (i in declared.indices) {
                if (i < flags.size && (flags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    granted.add(declared[i])
                }
            }

            val sensitiveItems = PermissionAnalyzer.analyze(pkg)

            val specialFlags = mutableListOf<String>()
            if (declared.contains("android.permission.SYSTEM_ALERT_WINDOW")) specialFlags.add("Draw Over Other Apps (Overlay)")
            if (declared.contains("android.permission.REQUEST_INSTALL_PACKAGES")) specialFlags.add("Can Request Package Installation")
            if (declared.contains("android.permission.PACKAGE_USAGE_STATS")) specialFlags.add("Can Access App Usage History")
            if (declared.contains("android.permission.MANAGE_EXTERNAL_STORAGE")) specialFlags.add("All Files Access")
            if (declared.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")) specialFlags.add("Declares Accessibility Service")
            if (declared.contains("android.permission.BIND_DEVICE_ADMIN")) specialFlags.add("Declares Device Administrator")

            val isAccessibilityActive = activeAccessibilityPackages.contains(pkg.packageName)
            val isDeviceAdminActive = activeDeviceAdminPackages.contains(pkg.packageName)

            if (isAccessibilityActive) specialFlags.add("Active Accessibility Service")
            if (isDeviceAdminActive) specialFlags.add("Active Device Administrator")

            // Known Stalkerware IoC Matching
            val iocMatch = StalkerwareIocDatabase.findMatch(pkg.packageName, certSha256)

            // Preliminary item for threat scoring
            val initialItem = AppAuditItem(
                packageName = pkg.packageName,
                appName = appLabel,
                versionName = pkg.versionName ?: "1.0",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pkg.longVersionCode else @Suppress("DEPRECATION") pkg.versionCode.toLong(),
                isSystemApp = isSystem,
                installerPackage = installerPackage,
                installerLabel = installerLabel,
                isUnknownSource = isUnknownSource,
                declaredPermissions = declared,
                grantedPermissions = granted,
                sensitivePermissions = sensitiveItems,
                specialAccessFlags = specialFlags,
                maxSeverity = Severity.INFORMATIONAL,
                findingsCount = 0,
                hasLauncherActivity = hasLauncher,
                signingCertSha256 = certSha256,
                initiatingPackage = initiatingPackage,
                originatingPackage = originatingPackage,
                iocMatch = iocMatch
            )

            // Threat Scoring Evaluation
            val threatResult = scoringEngine.evaluate(
                app = initialItem,
                isAccessibilityServiceActive = isAccessibilityActive,
                isDeviceAdminActive = isDeviceAdminActive
            )

            val maxSev = determineAppMaxSeverity(
                grantedSensitives = sensitiveItems.count { it.isGranted },
                isUnknown = isUnknownSource,
                specialFlagsCount = specialFlags.size,
                threatScore = threatResult.score,
                hasIocMatch = iocMatch != null
            )

            initialItem.copy(
                maxSeverity = maxSev,
                findingsCount = (if (isUnknownSource) 1 else 0) +
                        sensitiveItems.count { it.isGranted } +
                        specialFlags.size +
                        threatResult.indicators.size +
                        (if (iocMatch != null) 1 else 0),
                threatScore = threatResult.score,
                threatCategory = threatResult.category,
                contributingThreatIndicators = threatResult.indicators
            )
        }
    }

    private fun getActiveAccessibilityPackages(): Set<String> {
        val result = mutableSetOf<String>()
        try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val enabled = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            enabled?.forEach { service ->
                val pkg = service.resolveInfo?.serviceInfo?.packageName ?: service.id?.substringBefore("/")
                if (!pkg.isNullOrEmpty()) result.add(pkg)
            }
        } catch (_: Exception) {}
        return result
    }

    private fun getActiveDeviceAdminPackages(): Set<String> {
        val result = mutableSetOf<String>()
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            dpm?.activeAdmins?.forEach { admin ->
                result.add(admin.packageName)
            }
        } catch (_: Exception) {}
        return result
    }

    private fun getInstallSourceDetails(packageName: String): Triple<String?, String?, String?> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val source = packageManager.getInstallSourceInfo(packageName)
                return Triple(
                    source.installingPackageName,
                    source.initiatingPackageName,
                    source.originatingPackageName
                )
            } catch (_: Exception) {}
        }
        @Suppress("DEPRECATION")
        val installer = try { packageManager.getInstallerPackageName(packageName) } catch (_: Exception) { null }
        return Triple(installer, null, null)
    }

    private fun extractCertSha256(pkg: PackageInfo): String? {
        return try {
            val certs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = pkg.signingInfo ?: return null
                if (info.hasMultipleSigners()) info.apkContentsSigners else info.signingCertificateHistory
            } else {
                @Suppress("DEPRECATION")
                pkg.signatures
            }
            val firstCert = certs?.firstOrNull()?.toByteArray() ?: return null
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(firstCert)
            digest.joinToString(":") { "%02X".format(it) }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveInstallerLabel(installerPackage: String?, isSystem: Boolean): String {
        if (installerPackage == null) {
            return if (isSystem) "System ROM" else "Unknown / Sideloaded"
        }
        return when (installerPackage) {
            "com.android.vending" -> "Google Play Store"
            "com.google.android.packageinstaller" -> "Android Package Installer"
            "com.sec.android.app.samsungapps" -> "Galaxy Store"
            "com.amazon.venezia" -> "Amazon Appstore"
            "org.fdroid.fdroid" -> "F-Droid Open Source Repository"
            else -> installerPackage
        }
    }

    private fun determineAppMaxSeverity(
        grantedSensitives: Int,
        isUnknown: Boolean,
        specialFlagsCount: Int,
        threatScore: Int,
        hasIocMatch: Boolean
    ): Severity {
        return when {
            hasIocMatch -> Severity.CRITICAL
            threatScore >= 60 -> Severity.HIGH
            threatScore >= 40 || (isUnknown && grantedSensitives >= 3) -> Severity.HIGH
            threatScore >= 20 || isUnknown || specialFlagsCount >= 2 -> Severity.MEDIUM
            grantedSensitives >= 2 -> Severity.LOW
            else -> Severity.INFORMATIONAL
        }
    }

    companion object {
        val KNOWN_SAFE_INSTALLERS = setOf(
            "com.android.vending",
            "com.google.android.packageinstaller",
            "com.sec.android.app.samsungapps",
            "com.amazon.venezia"
        )
    }
}
