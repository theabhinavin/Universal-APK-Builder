package com.example.domain.model

enum class ScanStage(val title: String, val description: String) {
    DEVICE_CONFIG("Device Configuration", "Checking OS, build metadata, and system security"),
    APP_INVENTORY("Application Inventory", "Enumerating installed applications and package scopes"),
    PERMISSION_REVIEW("Permission Review", "Analyzing manifest declarations and runtime grant states"),
    CAMERA_PRIVACY("Camera Privacy", "Auditing camera access rights and platform controls"),
    MICROPHONE_PRIVACY("Microphone Privacy", "Auditing audio recording permissions and toggles"),
    LOCATION_PRIVACY("Location Privacy", "Inspecting precise, coarse, and background location grants"),
    SPECIAL_ACCESS("Special Access", "Reviewing accessibility, device admin, overlay, and VPN privileges"),
    UNKNOWN_SOURCES("Installation Sources", "Checking installer package origins and sideload flags"),
    SECURITY_POSTURE("Security Posture", "Evaluating keyguard, debug settings, and platform boundaries")
}

enum class Severity(val label: String, val weight: Int) {
    CRITICAL("Critical", 5),
    HIGH("High", 4),
    MEDIUM("Medium", 3),
    LOW("Low", 2),
    INFORMATIONAL("Info", 1)
}

enum class AuditCategory(val displayName: String) {
    DEVICE("Device Posture"),
    APPLICATION("Installed App"),
    CAMERA("Camera Access"),
    MICROPHONE("Microphone Access"),
    LOCATION("Location Access"),
    SENSITIVE_PERMISSION("Sensitive Permission"),
    SPECIAL_ACCESS("Special Privileges"),
    INSTALLER("Installation Origin"),
    SECURITY("System Security")
}
