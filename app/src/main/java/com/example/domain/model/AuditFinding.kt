package com.example.domain.model

data class AuditFinding(
    val id: String,
    val title: String,
    val severity: Severity,
    val category: AuditCategory,
    val affectedPackage: String? = null,
    val affectedAppName: String? = null,
    val evidence: String,
    val explanation: String,
    val limitations: String,
    val recommendedAction: String,
    val systemIntentAction: String? = null,
    val systemIntentPackage: String? = null
)

data class SensitivePermissionItem(
    val permission: String,
    val group: String,
    val isGranted: Boolean,
    val isBackground: Boolean = false,
    val description: String
)

enum class SpecialAccessCategory(val title: String) {
    ACCESSIBILITY("Accessibility Service"),
    DEVICE_ADMIN("Device Administrator"),
    VPN("Active VPN Connection"),
    NOTIFICATION_LISTENER("Notification Listener"),
    SYSTEM_ALERT_WINDOW("Draw Over Other Apps"),
    PACKAGE_USAGE_STATS("Usage Access"),
    REQUEST_INSTALL_PACKAGES("Install Unknown Apps"),
    MANAGE_EXTERNAL_STORAGE("All Files Access")
}

data class SpecialAccessItem(
    val category: SpecialAccessCategory,
    val title: String,
    val appName: String,
    val packageName: String,
    val status: String,
    val riskExplanation: String,
    val recommendedAction: String,
    val settingsIntentAction: String
)
