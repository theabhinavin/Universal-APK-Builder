package com.example.scanner

import android.content.pm.PackageInfo
import com.example.domain.model.SensitivePermissionItem

object PermissionAnalyzer {

    data class PermissionDef(
        val permission: String,
        val group: String,
        val isBackground: Boolean = false,
        val description: String
    )

    private val SENSITIVE_DEFINITIONS = listOf(
        // Camera
        PermissionDef(
            "android.permission.CAMERA",
            "Camera",
            false,
            "Captures photos, video recordings, and optical sensor data."
        ),
        // Microphone
        PermissionDef(
            "android.permission.RECORD_AUDIO",
            "Microphone",
            false,
            "Captures environmental audio and voice conversations."
        ),
        // Location
        PermissionDef(
            "android.permission.ACCESS_FINE_LOCATION",
            "Location",
            false,
            "Accesses precise GPS and satellite device coordinates."
        ),
        PermissionDef(
            "android.permission.ACCESS_COARSE_LOCATION",
            "Location",
            false,
            "Accesses approximate network/cell tower geographic position."
        ),
        PermissionDef(
            "android.permission.ACCESS_BACKGROUND_LOCATION",
            "Location",
            true,
            "Continues tracking device position even when app is closed or in background."
        ),
        // Contacts
        PermissionDef(
            "android.permission.READ_CONTACTS",
            "Contacts",
            false,
            "Reads address book contacts, email addresses, and phone numbers."
        ),
        PermissionDef(
            "android.permission.WRITE_CONTACTS",
            "Contacts",
            false,
            "Creates, modifies, or deletes entries in user address book."
        ),
        // Phone & Calls
        PermissionDef(
            "android.permission.READ_PHONE_STATE",
            "Phone & Calls",
            false,
            "Reads cellular network state, carrier, and ongoing phone calls."
        ),
        PermissionDef(
            "android.permission.READ_CALL_LOG",
            "Phone & Calls",
            false,
            "Reads historical incoming, outgoing, and missed call logs."
        ),
        PermissionDef(
            "android.permission.PROCESS_OUTGOING_CALLS",
            "Phone & Calls",
            false,
            "Inspects or redirects dialed outgoing calls."
        ),
        PermissionDef(
            "android.permission.CALL_PHONE",
            "Phone & Calls",
            false,
            "Initiates telephone calls directly without dialer confirmation."
        ),
        // SMS
        PermissionDef(
            "android.permission.READ_SMS",
            "SMS & Messages",
            false,
            "Reads stored SMS text messages, including 2FA verification codes."
        ),
        PermissionDef(
            "android.permission.RECEIVE_SMS",
            "SMS & Messages",
            false,
            "Intercepts incoming text messages in real-time."
        ),
        PermissionDef(
            "android.permission.SEND_SMS",
            "SMS & Messages",
            false,
            "Sends SMS messages directly, potentially incurring carrier charges."
        ),
        // Calendar
        PermissionDef(
            "android.permission.READ_CALENDAR",
            "Calendar",
            false,
            "Reads personal events, meeting schedules, and attendee lists."
        ),
        PermissionDef(
            "android.permission.WRITE_CALENDAR",
            "Calendar",
            false,
            "Creates or edits calendar events."
        ),
        // Nearby / Bluetooth
        PermissionDef(
            "android.permission.BLUETOOTH_SCAN",
            "Nearby Devices",
            false,
            "Scans for nearby Bluetooth beacons, trackers, and peripherals."
        ),
        PermissionDef(
            "android.permission.BLUETOOTH_CONNECT",
            "Nearby Devices",
            false,
            "Connects to paired Bluetooth devices and audio accessories."
        ),
        PermissionDef(
            "android.permission.NEARBY_WIFI_DEVICES",
            "Nearby Devices",
            false,
            "Discovers and connects to nearby Wi-Fi devices without location."
        ),
        // Sensors
        PermissionDef(
            "android.permission.BODY_SENSORS",
            "Body Sensors",
            false,
            "Reads heart rate, biometrics, and physical biometric sensors."
        ),
        PermissionDef(
            "android.permission.ACTIVITY_RECOGNITION",
            "Physical Activity",
            false,
            "Recognizes walking, running, driving, or step count activity."
        ),
        // Notifications
        PermissionDef(
            "android.permission.POST_NOTIFICATIONS",
            "Notifications",
            false,
            "Posts alerts, status updates, and notifications to device shade."
        ),
        // Overlay / Draw on top
        PermissionDef(
            "android.permission.SYSTEM_ALERT_WINDOW",
            "Special System Access",
            false,
            "Displays overlay windows over other applications, with potential tapjacking, screen capture, or deceptive phishing risks."
        )
    )

    fun analyze(packageInfo: PackageInfo): List<SensitivePermissionItem> {
        val requested = packageInfo.requestedPermissions ?: return emptyList()
        val flags = packageInfo.requestedPermissionsFlags ?: IntArray(requested.size)

        val result = mutableListOf<SensitivePermissionItem>()

        for (i in requested.indices) {
            val permName = requested[i]
            val isGranted = if (i < flags.size) {
                (flags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            } else false

            val def = SENSITIVE_DEFINITIONS.find { it.permission.equals(permName, ignoreCase = true) }
            if (def != null) {
                result.add(
                    SensitivePermissionItem(
                        permission = permName,
                        group = def.group,
                        isGranted = isGranted,
                        isBackground = def.isBackground,
                        description = def.description
                    )
                )
            }
        }
        return result
    }

    fun isSensitivePermission(permission: String): Boolean {
        return SENSITIVE_DEFINITIONS.any { it.permission.equals(permission, ignoreCase = true) }
    }
}
