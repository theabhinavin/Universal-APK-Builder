package com.example.scanner

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.example.domain.model.DeviceSecurityInfo
import java.io.File

class DeviceScanner(private val context: Context) {

    fun scanDevice(): DeviceSecurityInfo {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isDeviceSecure = keyguardManager?.isDeviceSecure ?: false

        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val isEncrypted = try {
            val status = dpm?.storageEncryptionStatus ?: DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE
            status == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE ||
                    status == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY ||
                    status == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER
        } catch (_: Exception) {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q // Android 10+ enforces encryption by default
        }

        val isDevOptions = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        } catch (_: Exception) {
            false
        }

        val isAdb = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Exception) {
            false
        }

        val rootHeuristics = checkRootHeuristics()

        return DeviceSecurityInfo(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            brand = Build.BRAND,
            device = Build.DEVICE,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Build.VERSION.SECURITY_PATCH
            } else "N/A",
            isDeviceSecure = isDeviceSecure,
            isDeveloperOptionsEnabled = isDevOptions,
            isAdbEnabled = isAdb,
            rootHeuristicsDetected = rootHeuristics,
            rootHeuristicsLimitations = "Root detection uses non-privileged file and build-tag heuristics. It cannot detect stealth modifications or guarantee complete absence of compromise.",
            isDeviceEncrypted = isEncrypted
        )
    }

    private fun checkRootHeuristics(): List<String> {
        val detected = mutableListOf<String>()

        // 1. Build Tags check
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            detected.add("Custom ROM / Test-Keys build tags detected")
        }

        // 2. Common SU binary paths
        val suPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in suPaths) {
            try {
                if (File(path).exists()) {
                    detected.add("SU binary found at $path")
                    break
                }
            } catch (_: Exception) {
                // Restricted by SEAndroid sandbox
            }
        }

        return detected
    }
}
