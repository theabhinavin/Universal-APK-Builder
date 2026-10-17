package com.example.scanner

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.example.domain.model.SpecialAccessCategory
import com.example.domain.model.SpecialAccessItem

class SpecialAccessScanner(private val context: Context) {

    fun scanSpecialAccess(): List<SpecialAccessItem> {
        val items = mutableListOf<SpecialAccessItem>()

        // 1. Accessibility Services
        try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val enabledServices = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            enabledServices?.forEach { serviceInfo ->
                val serviceResolveInfo = serviceInfo.resolveInfo
                val pkgName = serviceResolveInfo?.serviceInfo?.packageName ?: serviceInfo.id ?: "Unknown"
                val appLabel = serviceResolveInfo?.loadLabel(context.packageManager)?.toString() ?: pkgName

                items.add(
                    SpecialAccessItem(
                        category = SpecialAccessCategory.ACCESSIBILITY,
                        title = "Enabled Accessibility Service",
                        appName = appLabel,
                        packageName = pkgName,
                        status = "Active Service",
                        riskExplanation = "Accessibility services can read on-screen content, intercept keystrokes, and interact with other applications on your behalf.",
                        recommendedAction = "Ensure you explicitly installed and trust this accessibility tool. Disable if unfamiliar.",
                        settingsIntentAction = Settings.ACTION_ACCESSIBILITY_SETTINGS
                    )
                )
            }
        } catch (_: Exception) {
            // Handled
        }

        // 2. Device Administrators
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            val activeAdmins = dpm?.activeAdmins
            activeAdmins?.forEach { adminComponent ->
                val pkgName = adminComponent.packageName
                val appLabel = try {
                    val appInfo = context.packageManager.getApplicationInfo(pkgName, 0)
                    context.packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    pkgName
                }

                items.add(
                    SpecialAccessItem(
                        category = SpecialAccessCategory.DEVICE_ADMIN,
                        title = "Active Device Administrator",
                        appName = appLabel,
                        packageName = pkgName,
                        status = "Device Administrator",
                        riskExplanation = "Device admins can enforce lock policies, wipe device data, and prevent application uninstallation.",
                        recommendedAction = "Verify if this admin is associated with company enterprise management or a personal anti-theft tool.",
                        settingsIntentAction = "android.settings.SECURITY_SETTINGS"
                    )
                )
            }
        } catch (_: Exception) {
            // Handled
        }

        // 3. Active VPN Connection
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(activeNetwork)
            val isVpnActive = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false

            if (isVpnActive) {
                items.add(
                    SpecialAccessItem(
                        category = SpecialAccessCategory.VPN,
                        title = "Active VPN Connection",
                        appName = "System VPN Tunnel",
                        packageName = "android.net.vpn",
                        status = "Active Tunnel",
                        riskExplanation = "A VPN tunnel routes device network traffic through an intermediary endpoint. The VPN operator can inspect or modify unencrypted traffic.",
                        recommendedAction = "Confirm you initiated this VPN connection via a trusted provider.",
                        settingsIntentAction = Settings.ACTION_VPN_SETTINGS
                    )
                )
            }
        } catch (_: Exception) {
            // Handled
        }

        // 4. Notification Listeners
        try {
            val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            if (!listeners.isNullOrEmpty()) {
                val components = listeners.split(":")
                components.forEach { comp ->
                    val pkg = comp.substringBefore("/")
                    if (pkg.isNotEmpty() && pkg != context.packageName) {
                        items.add(
                            SpecialAccessItem(
                                category = SpecialAccessCategory.NOTIFICATION_LISTENER,
                                title = "Notification Access",
                                appName = pkg,
                                packageName = pkg,
                                status = "Listener Enabled",
                                riskExplanation = "Notification listeners can intercept incoming text messages, two-factor auth tokens, and message contents.",
                                recommendedAction = "Review which apps are allowed to inspect notification streams.",
                                settingsIntentAction = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Handled
        }

        // 5. Overlay Windows (SYSTEM_ALERT_WINDOW)
        try {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            packages.forEach { pkg ->
                val requested = pkg.requestedPermissions ?: return@forEach
                val flags = pkg.requestedPermissionsFlags ?: IntArray(requested.size)
                for (i in requested.indices) {
                    if (requested[i] == "android.permission.SYSTEM_ALERT_WINDOW") {
                        val isGranted = if (i < flags.size) (flags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0 else false
                        val isSystem = (pkg.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) ?: 0) != 0
                        if (!isSystem && isGranted) {
                            val appLabel = try {
                                pm.getApplicationLabel(pkg.applicationInfo ?: return@forEach).toString()
                            } catch (_: Exception) {
                                pkg.packageName
                            }
                            items.add(
                                SpecialAccessItem(
                                    category = SpecialAccessCategory.SYSTEM_ALERT_WINDOW,
                                    title = "Granted Overlay Permission",
                                    appName = appLabel,
                                    packageName = pkg.packageName,
                                    status = "Can Draw Over Other Apps",
                                    riskExplanation = "Overlay windows can obscure banking screens, intercept touch inputs (tapjacking), or capture on-screen confidential details.",
                                    recommendedAction = "Verify if this application requires floating UI or drawing over other applications.",
                                    settingsIntentAction = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Handled
        }

        return items
    }
}
