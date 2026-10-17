package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppAuditItem
import com.example.domain.model.DeviceSecurityInfo
import com.example.domain.model.ScanReport
import com.example.ui.components.launchSystemSetting
import com.example.ui.theme.AbmCyanPrimary
import com.example.ui.theme.AbmDarkBorder
import com.example.ui.theme.AbmDarkNavyBg
import com.example.ui.theme.AbmDarkSurface
import com.example.ui.theme.AbmDarkSurfaceVariant
import com.example.ui.theme.AbmPostureSafe
import com.example.ui.theme.AbmSeverityCritical
import com.example.ui.theme.AbmSeverityHigh
import com.example.ui.theme.AbmSeverityMedium
import com.example.ui.theme.AbmTextPrimary
import com.example.ui.theme.AbmTextSecondary
import com.example.ui.theme.AbmTextTertiary
import com.example.ui.theme.AbmVioletSecondary
import com.example.ui.viewmodel.ScanUiState

// -------------------------------------------------------------
// MODULE 1: Device Overview Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceOverviewScreen(state: ScanUiState, onBack: () -> Unit) {
    BackHandler { onBack() }
    val dev = state.report?.deviceInfo

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = { ModuleTopBar(title = "Device Overview", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("HARDWARE & FIRMWARE", color = AbmCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            item {
                InfoCard(label = "Manufacturer", value = dev?.manufacturer ?: Build.MANUFACTURER.replaceFirstChar { it.uppercase() })
            }
            item {
                InfoCard(label = "Model", value = dev?.model ?: Build.MODEL)
            }
            item {
                InfoCard(label = "Android Version", value = "Android ${dev?.androidVersion ?: Build.VERSION.RELEASE} (API ${dev?.apiLevel ?: Build.VERSION.SDK_INT})")
            }
            item {
                InfoCard(label = "Security Patch Level", value = dev?.securityPatch ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A")
            }
            item {
                Text("SECURITY STATE", color = AbmCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                InfoCard(
                    label = "Screen Lock Protection",
                    value = if (dev?.isDeviceSecure == true) "Configured (PIN / Biometrics Active)" else "UNPROTECTED (No Screen Lock)",
                    valueColor = if (dev?.isDeviceSecure == true) AbmPostureSafe else AbmSeverityHigh
                )
            }
            item {
                InfoCard(
                    label = "Storage Encryption",
                    value = if (dev?.isDeviceEncrypted == true) "Encrypted" else "Unknown / Unencrypted",
                    valueColor = if (dev?.isDeviceEncrypted == true) AbmPostureSafe else AbmSeverityMedium
                )
            }
            item {
                InfoCard(
                    label = "Developer Options",
                    value = if (dev?.isDeveloperOptionsEnabled == true) "Active" else "Disabled (Default)",
                    valueColor = if (dev?.isDeveloperOptionsEnabled == true) AbmSeverityMedium else AbmPostureSafe
                )
            }
            item {
                InfoCard(
                    label = "USB Debugging (ADB)",
                    value = if (dev?.isAdbEnabled == true) "Enabled" else "Disabled (Default)",
                    valueColor = if (dev?.isAdbEnabled == true) AbmSeverityHigh else AbmPostureSafe
                )
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// -------------------------------------------------------------
// MODULE 3: Camera Privacy Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPrivacyScreen(state: ScanUiState, onSelectApp: (AppAuditItem) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val appsWithCamera = state.report?.apps?.filter { app ->
        app.sensitivePermissions.any { it.group == "Camera" && it.isGranted }
    } ?: emptyList()

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = { ModuleTopBar(title = "Camera Privacy", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ModuleIntroCard(
                    icon = Icons.Default.CameraAlt,
                    title = "${appsWithCamera.size} Apps Have Camera Permission",
                    description = "Permission grants technical ability to open optical sensors. It does NOT mean the camera is actively filming. Android 12+ shows a live green dot indicator in the top right whenever any app accesses camera sensors.",
                    actionLabel = "Open System Privacy Dashboard",
                    onAction = { launchSystemSetting(context, "android.settings.PRIVACY_SETTINGS") }
                )
            }
            item {
                Text("APPS WITH ACTIVE CAMERA GRANT", color = AbmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            if (appsWithCamera.isEmpty()) {
                item { EmptyAuditBox("No applications have active camera grants.") }
            } else {
                items(appsWithCamera) { app ->
                    AppPermissionCard(app = app, permGroup = "Camera", onClick = { onSelectApp(app) })
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// -------------------------------------------------------------
// MODULE 4: Microphone Privacy Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicrophonePrivacyScreen(state: ScanUiState, onSelectApp: (AppAuditItem) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val appsWithMic = state.report?.apps?.filter { app ->
        app.sensitivePermissions.any { it.group == "Microphone" && it.isGranted }
    } ?: emptyList()

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = { ModuleTopBar(title = "Microphone Privacy", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ModuleIntroCard(
                    icon = Icons.Default.Mic,
                    title = "${appsWithMic.size} Apps Have Audio Recording Access",
                    description = "Microphone permissions allow capturing environmental audio. Android does not expose private historical access logs to normal apps. Use the Privacy Dashboard to view 24-hour access timelines.",
                    actionLabel = "Open Privacy Dashboard",
                    onAction = { launchSystemSetting(context, "android.settings.PRIVACY_SETTINGS") }
                )
            }
            item {
                Text("APPS WITH ACTIVE MICROPHONE GRANT", color = AbmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            if (appsWithMic.isEmpty()) {
                item { EmptyAuditBox("No applications have active audio recording grants.") }
            } else {
                items(appsWithMic) { app ->
                    AppPermissionCard(app = app, permGroup = "Microphone", onClick = { onSelectApp(app) })
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// -------------------------------------------------------------
// MODULE 5: Location Privacy Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPrivacyScreen(state: ScanUiState, onSelectApp: (AppAuditItem) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val appsWithLoc = state.report?.apps?.filter { app ->
        app.sensitivePermissions.any { it.group == "Location" && it.isGranted }
    } ?: emptyList()

    val bgLocApps = appsWithLoc.filter { app ->
        app.sensitivePermissions.any { it.isBackground && it.isGranted }
    }

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = { ModuleTopBar(title = "Location Privacy", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ModuleIntroCard(
                    icon = Icons.Default.LocationOn,
                    title = "${appsWithLoc.size} Apps Have Location Access (${bgLocApps.size} in Background)",
                    description = "Precise location (GPS) provides exact physical coordinates, while Coarse location provides approximate cell-tower position. Background location allows tracking when the app is minimized.",
                    actionLabel = "Open Location Settings",
                    onAction = { launchSystemSetting(context, Settings.ACTION_LOCATION_SOURCE_SETTINGS) }
                )
            }
            if (bgLocApps.isNotEmpty()) {
                item {
                    Text("BACKGROUND LOCATION GRANTED (CRITICAL REVIEW)", color = AbmSeverityHigh, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                items(bgLocApps) { app ->
                    AppPermissionCard(app = app, permGroup = "Background Location", onClick = { onSelectApp(app) }, isHighlight = true)
                }
            }
            item {
                Text("ALL LOCATION ACCESS GRANTS", color = AbmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            if (appsWithLoc.isEmpty()) {
                item { EmptyAuditBox("No applications have active location permissions.") }
            } else {
                items(appsWithLoc) { app ->
                    AppPermissionCard(app = app, permGroup = "Location", onClick = { onSelectApp(app) })
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// -------------------------------------------------------------
// MODULE 6: Sensitive Permissions Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensitivePermissionsScreen(state: ScanUiState, onSelectApp: (AppAuditItem) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    val apps = state.report?.apps ?: emptyList()

    val categories = listOf("Camera", "Microphone", "Location", "Contacts", "Phone & Calls", "SMS & Messages", "Calendar", "Nearby Devices", "Body Sensors", "Notifications")

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = { ModuleTopBar(title = "Sensitive Permissions", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("10 SENSITIVE PRIVACY DOMAINS", color = AbmCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sensitive permissions protect private personal data and physical device sensors. Review which applications hold active access to each domain.",
                            color = AbmTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            items(categories) { cat ->
                val matchingApps = apps.filter { app ->
                    app.sensitivePermissions.any { it.group == cat && it.isGranted }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = AbmDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = cat, color = AbmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Surface(shape = RoundedCornerShape(4.dp), color = if (matchingApps.isEmpty()) Color(0x1FFFFFFF) else Color(0x3300E5FF)) {
                                Text(
                                    text = "${matchingApps.size} apps",
                                    color = if (matchingApps.isEmpty()) AbmTextTertiary else AbmCyanPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (matchingApps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = matchingApps.take(4).joinToString(", ") { it.appName } + if (matchingApps.size > 4) " +${matchingApps.size - 4} more" else "",
                                color = AbmTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// -------------------------------------------------------------
// MODULE 7: Special Access Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialAccessScreen(state: ScanUiState, onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val specialFindings = state.report?.findings?.filter { it.category == com.example.domain.model.AuditCategory.SPECIAL_ACCESS } ?: emptyList()

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = { ModuleTopBar(title = "Special App Access", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ModuleIntroCard(
                    icon = Icons.Default.Shield,
                    title = "Elevated Platform Capabilities",
                    description = "Special access permissions bypass standard app sandboxing. These include Accessibility Services, Device Administrators, Active VPNs, and Notification Listeners.",
                    actionLabel = "Open Accessibility Settings",
                    onAction = { launchSystemSetting(context, Settings.ACTION_ACCESSIBILITY_SETTINGS) }
                )
            }
            item {
                Text("ACTIVE SPECIAL PRIVILEGES OBSERVED (${specialFindings.size})", color = AbmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            if (specialFindings.isEmpty()) {
                item { EmptyAuditBox("No high-privilege accessibility services, device administrators, or notification listeners currently active.") }
            } else {
                items(specialFindings) { finding ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = finding.title, color = AbmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (!finding.affectedAppName.isNullOrEmpty()) {
                                Text(text = "${finding.affectedAppName} (${finding.affectedPackage})", color = AbmCyanPrimary, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = finding.explanation, color = AbmTextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            if (finding.systemIntentAction != null) {
                                OutlinedButton(onClick = { launchSystemSetting(context, finding.systemIntentAction) }) {
                                    Text("Open Settings", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// -------------------------------------------------------------
// MODULE 8: Unknown Sources Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnknownSourcesScreen(state: ScanUiState, onSelectApp: (AppAuditItem) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    val unknownApps = state.report?.apps?.filter { it.isUnknownSource } ?: emptyList()

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = { ModuleTopBar(title = "Unknown Installation Sources", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFFFF9100), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sideloading & Installation Origins", color = AbmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Apps installed outside the Google Play Store or OEM store are classified as unknown or sideloaded. Sideloading can be completely legitimate (e.g. F-Droid, manual developer builds), but these apps bypass Play Protect automated verification.",
                            color = AbmTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
            item {
                Text("NON-STORE INSTALLED APPS (${unknownApps.size})", color = AbmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            if (unknownApps.isEmpty()) {
                item { EmptyAuditBox("All visible user apps were installed via recognized platform stores.") }
            } else {
                items(unknownApps) { app ->
                    AppPermissionCard(app = app, permGroup = "Sideloaded", onClick = { onSelectApp(app) }, isHighlight = true)
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// -------------------------------------------------------------
// MODULE 9: Security Posture Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityPostureScreen(state: ScanUiState, onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val dev = state.report?.deviceInfo
    val findings = state.report?.findings?.filter { it.category == com.example.domain.model.AuditCategory.SECURITY } ?: emptyList()

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = { ModuleTopBar(title = "Security Posture", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AbmCyanPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("System Integrity & Configuration", color = AbmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Audits device lock configuration, developer options, ADB debugging, and root heuristics observable through legitimate Android platform boundaries.",
                            color = AbmTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
            item {
                Text("POSTURE FINDINGS (${findings.size})", color = AbmTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            items(findings) { finding ->
                com.example.ui.components.FindingCard(finding = finding)
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// -------------------------------------------------------------
// Helper UI Components
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, color = AbmTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AbmTextPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AbmDarkNavyBg)
    )
}

@Composable
fun ModuleIntroCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AbmCyanPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, color = AbmTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, color = AbmTextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AbmCyanPrimary, contentColor = Color(0xFF002933)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(actionLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String, valueColor: Color = AbmTextPrimary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = AbmDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = AbmTextSecondary, fontSize = 13.sp)
            Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun AppPermissionCard(
    app: AppAuditItem,
    permGroup: String,
    onClick: () -> Unit,
    isHighlight: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = AbmDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isHighlight) AbmSeverityMedium.copy(alpha = 0.5f) else AbmDarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0x1F00E5FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(app.appName.take(1).uppercase(), color = AbmCyanPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.appName, color = AbmTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                Text(app.packageName, color = AbmTextTertiary, fontSize = 10.sp, maxLines = 1)
            }
            Surface(shape = RoundedCornerShape(4.dp), color = Color(0x3300E5FF)) {
                Text(permGroup, color = AbmCyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
fun EmptyAuditBox(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = AbmDarkSurface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = AbmPostureSafe, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, color = AbmTextSecondary, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
