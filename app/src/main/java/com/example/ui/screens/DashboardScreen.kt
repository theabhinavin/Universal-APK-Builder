package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AbmBrandHeader
import com.example.ui.theme.AbmCyanPrimary
import com.example.ui.theme.AbmDarkBorder
import com.example.ui.theme.AbmDarkNavyBg
import com.example.ui.theme.AbmDarkSurface
import com.example.ui.theme.AbmDarkSurfaceVariant
import com.example.ui.theme.AbmElectricBlue
import com.example.ui.theme.AbmPostureSafe
import com.example.ui.theme.AbmTextPrimary
import com.example.ui.theme.AbmTextSecondary
import com.example.ui.theme.AbmTextTertiary
import com.example.ui.theme.AbmVioletSecondary
import com.example.ui.viewmodel.ScanUiState
import com.example.ui.viewmodel.ScreenDestination
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AuditModuleNav(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val destination: ScreenDestination,
    val iconTint: Color
)

@Composable
fun DashboardScreen(
    state: ScanUiState,
    onStartScan: () -> Unit,
    onNavigate: (ScreenDestination) -> Unit
) {
    val modules = listOf(
        AuditModuleNav(
            title = "Installed Apps",
            description = "Inventory & permission grants",
            icon = Icons.Default.Apps,
            destination = ScreenDestination.MODULE_APPS,
            iconTint = AbmCyanPrimary
        ),
        AuditModuleNav(
            title = "Camera Privacy",
            description = "Camera grants & indicators",
            icon = Icons.Default.CameraAlt,
            destination = ScreenDestination.MODULE_CAMERA,
            iconTint = Color(0xFF00E676)
        ),
        AuditModuleNav(
            title = "Microphone Privacy",
            description = "Audio recording posture",
            icon = Icons.Default.Mic,
            destination = ScreenDestination.MODULE_MICROPHONE,
            iconTint = Color(0xFFFFB300)
        ),
        AuditModuleNav(
            title = "Location Privacy",
            description = "Background & fine tracking",
            icon = Icons.Default.LocationOn,
            destination = ScreenDestination.MODULE_LOCATION,
            iconTint = Color(0xFF38BDF8)
        ),
        AuditModuleNav(
            title = "Sensitive Perms",
            description = "Contacts, SMS, calls, sensors",
            icon = Icons.Default.Key,
            destination = ScreenDestination.MODULE_SENSITIVE_PERMISSIONS,
            iconTint = AbmVioletSecondary
        ),
        AuditModuleNav(
            title = "Special Access",
            description = "Accessibility, admins, overlays",
            icon = Icons.Default.Shield,
            destination = ScreenDestination.MODULE_SPECIAL_ACCESS,
            iconTint = Color(0xFFFF5252)
        ),
        AuditModuleNav(
            title = "Stalkerware Audit",
            description = "Spyware & surveillance engine",
            icon = Icons.Default.Shield,
            destination = ScreenDestination.MODULE_STALKERWARE,
            iconTint = Color(0xFFFF5252)
        ),
        AuditModuleNav(
            title = "Unknown Sources",
            description = "Sideloaded & non-Play apps",
            icon = Icons.Default.Download,
            destination = ScreenDestination.MODULE_UNKNOWN_SOURCES,
            iconTint = Color(0xFFFF9100)
        ),
        AuditModuleNav(
            title = "Security Posture",
            description = "Lock screen, root, debug status",
            icon = Icons.Default.Security,
            destination = ScreenDestination.MODULE_SECURITY_POSTURE,
            iconTint = AbmCyanPrimary
        ),
        AuditModuleNav(
            title = "Audit History",
            description = "Latest scan diff & records",
            icon = Icons.Default.History,
            destination = ScreenDestination.MODULE_HISTORY,
            iconTint = AbmTextSecondary
        ),
        AuditModuleNav(
            title = "Audit Report",
            description = "Export JSON & Android Share",
            icon = Icons.Default.Share,
            destination = ScreenDestination.REPORT_EXPORT,
            iconTint = AbmElectricBlue
        )
    )

    Scaffold(
        containerColor = AbmDarkNavyBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AbmBrandHeader(compact = true, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onNavigate(ScreenDestination.SETTINGS_ABOUT) },
                    modifier = Modifier.testTag("settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings and About",
                        tint = AbmTextSecondary
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Headline Section
            item {
                Column(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                    Text(
                        text = "Your Phone. Your Privacy.",
                        color = AbmTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Discover app permissions, review sensor access, and understand your device's privacy status.",
                        color = AbmTextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Primary CTA: ABM SMART SCAN
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF005B66), Color(0xFF0D254C))
                                )
                            )
                            .border(1.5.dp, AbmCyanPrimary, RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Radar,
                                        contentDescription = null,
                                        tint = AbmCyanPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "FULL PRIVACY AUDIT",
                                        color = AbmCyanPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x3300E5FF)
                                ) {
                                    Text(
                                        text = "9 STAGES",
                                        color = AbmCyanPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Comprehensive on-device inspection across all installed applications, sensitive sensors, and platform privileges.",
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onStartScan,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("smart_scan_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AbmCyanPrimary,
                                    contentColor = Color(0xFF002229)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ABM SMART SCAN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Surveillance Alert banner if high-risk stalkerware is detected
            if ((state.report?.stalkerwareHighRiskCount ?: 0) > 0 || (state.report?.stalkerwareIocMatchCount ?: 0) > 0) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigate(ScreenDestination.MODULE_STALKERWARE) }
                            .testTag("stalkerware_alert_banner"),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x33FF3B30),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Surveillance Risks Detected",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${state.report?.stalkerwareHighRiskCount} high-threat app(s) or IoC matches found. Tap to inspect.",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = AbmCyanPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Compact Device Summary Card
            item {
                DeviceSummaryCard(state = state)
            }

            // Modules Grid Header
            item {
                Text(
                    text = "AUDIT MODULES",
                    color = AbmTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 10 Modules in clean rows/cards
            items(modules.chunked(2).size) { rowIndex ->
                val chunk = modules.chunked(2)[rowIndex]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    chunk.forEach { module ->
                        ModuleCard(
                            module = module,
                            onClick = { onNavigate(module.destination) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (chunk.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun DeviceSummaryCard(state: ScanUiState) {
    val cached = state.cachedEntity
    val report = state.report

    val lastScanTimeStr = when {
        report != null -> SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(report.timestamp))
        cached != null -> SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(cached.timestamp))
        else -> "Not scanned yet"
    }

    val statusText = when {
        report != null -> "Audited (${report.totalAppsScanned} apps, Score: ${report.overallRiskScore})"
        cached != null -> "Cached (${cached.totalApps} apps, Score: ${cached.riskScore})"
        else -> "First scan pending"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = AbmCyanPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
                        color = AbmTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AbmDarkSurfaceVariant
                ) {
                    Text(
                        text = "Android ${Build.VERSION.RELEASE}",
                        color = AbmVioletSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "SECURITY PATCH", color = AbmTextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A",
                        color = AbmTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text(text = "LAST SCAN", color = AbmTextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = lastScanTimeStr, color = AbmTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text(text = "STATUS", color = AbmTextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (report != null || cached != null) "Ready" else "New",
                        color = if (report != null || cached != null) AbmPostureSafe else AbmCyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ModuleCard(
    module: AuditModuleNav,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("module_card_${module.destination.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AbmDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AbmDarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(module.iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = module.title,
                    tint = module.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = module.title,
                color = AbmTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = module.description,
                color = AbmTextTertiary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2
            )
        }
    }
}
